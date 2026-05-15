-- migrate:up
CREATE TABLE users (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    email          VARCHAR(255)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255)  NOT NULL,
    first_name     VARCHAR(100)  NOT NULL,
    last_name      VARCHAR(100)  NOT NULL,
    phone          VARCHAR(20)   UNIQUE,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

COMMENT ON TABLE  users               IS 'Registered store customers.';
COMMENT ON COLUMN users.email         IS 'Unique email address used for login.';
COMMENT ON COLUMN users.password_hash IS 'Hash of the user''s password. Never returned in API responses.';
COMMENT ON COLUMN users.phone         IS 'Optional contact phone in E.164 format (e.g. +380501234567).';

CREATE TRIGGER set_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION auth.set_updated_at();

-- Expose user profiles without password_hash.
GRANT SELECT (id, email, first_name, last_name, phone, created_at, updated_at) ON users TO authenticated;
GRANT UPDATE (email, first_name, last_name, phone) ON users TO authenticated;

ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- USING filters rows on SELECT/UPDATE/DELETE (non-matching rows are silently invisible).
-- WITH CHECK rejects INSERT/UPDATE that would produce a row failing the condition.
CREATE POLICY users_own ON users
  FOR ALL TO authenticated
  USING      (id = current_setting('auth.user_id', true)::uuid)
  WITH CHECK (id = current_setting('auth.user_id', true)::uuid);

-- Returns a JWT on success; raises an exception on bad credentials.
CREATE OR REPLACE FUNCTION login(email text, password text)
RETURNS TABLE (token text)
-- SECURITY DEFINER: runs as the function owner so it can read password_hash and call sign().
-- SET search_path: prevents a malicious caller from shadowing crypt/sign with their own functions.
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
  _user    users;
  _payload json;
BEGIN
  SELECT * INTO _user FROM users u WHERE u.email = login.email;

  IF NOT FOUND OR _user.password_hash != crypt(login.password, _user.password_hash) THEN
    RAISE EXCEPTION 'Invalid email or password' USING ERRCODE = 'invalid_password';
  END IF;

  _payload := json_build_object(
    'role',    'authenticated',
    'user_id', _user.id,
    'exp',     extract(epoch FROM now() + interval '8 hours')::integer
  );

  RETURN QUERY SELECT sign(_payload, current_setting('app.jwt_secret'));
END;
$$;

-- Creates a new user and returns a JWT so the caller is immediately logged in.
CREATE OR REPLACE FUNCTION register(
  email      text,
  password   text,
  first_name text,
  last_name  text,
  phone      text DEFAULT NULL
)
RETURNS TABLE (token text)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
  _user    users;
  _payload json;
BEGIN
  INSERT INTO users (email, password_hash, first_name, last_name, phone)
  VALUES (
    register.email,
    crypt(register.password, gen_salt('bf')),
    register.first_name,
    register.last_name,
    register.phone
  )
  RETURNING * INTO _user;

  _payload := json_build_object(
    'role',    'authenticated',
    'user_id', _user.id,
    'exp',     extract(epoch FROM now() + interval '8 hours')::integer
  );

  RETURN QUERY SELECT sign(_payload, current_setting('app.jwt_secret'));
END;
$$;

COMMENT ON FUNCTION login(text, text) IS
'Authenticate with email and password. Returns a JWT valid for 8 hours.';

COMMENT ON FUNCTION register(text, text, text, text, text) IS
'Create a new customer account. Returns a JWT valid for 8 hours.';

-- PostgreSQL grants EXECUTE to PUBLIC by default on new functions; undo that.
REVOKE EXECUTE ON FUNCTION login(text, text)                      FROM PUBLIC;
REVOKE EXECUTE ON FUNCTION register(text, text, text, text, text) FROM PUBLIC;
-- Both anon and authenticated can log in, but only anon can register
GRANT  EXECUTE ON FUNCTION login(text, text)                      TO anon, authenticated;
GRANT  EXECUTE ON FUNCTION register(text, text, text, text, text) TO anon;

-- migrate:down
DROP FUNCTION IF EXISTS register(text, text, text, text, text);
DROP FUNCTION IF EXISTS login(text, text);
DROP TABLE IF EXISTS users;

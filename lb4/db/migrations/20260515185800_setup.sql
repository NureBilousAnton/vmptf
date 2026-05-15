-- migrate:up
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Holds internal helpers. PostgREST only exposes public schema.
CREATE SCHEMA auth;

COMMENT ON SCHEMA public IS $$
# Internet Shop API
REST API for the internet shop. Authenticate via POST /rpc/login or POST /rpc/register to receive a JWT.
$$;

-- pgjwt must live in public so its functions can call pgcrypto's hmac().
CREATE EXTENSION IF NOT EXISTS pgjwt;

-- Strip the default PUBLIC execute grant from every function the extensions just installed.
REVOKE EXECUTE ON ALL FUNCTIONS IN SCHEMA public FROM PUBLIC;

-- Called by PostgREST before every request (PGRST_DB_PRE_REQUEST).
CREATE OR REPLACE FUNCTION auth.set_auth_context() RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    _user_id text;
BEGIN
    -- Pull user_id out of the already-validated JWT claims and store it as a
    -- transaction-local variable so RLS policies can use it without re-parsing JSON.
    -- true = return NULL instead of raising an error when the variable is not set (unauthenticated requests)
    _user_id := current_setting('request.jwt.claims', true)::jsonb->>'user_id';
    IF _user_id IS NOT NULL THEN
        -- true = transaction-local, cleared automatically when the transaction ends
        PERFORM set_config('auth.user_id', _user_id, true);
    END IF;
END;
$$;

CREATE OR REPLACE FUNCTION auth.set_updated_at() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$$;

CREATE ROLE anon          NOLOGIN;
CREATE ROLE authenticated NOLOGIN;
-- NOINHERIT: authenticator does not automatically gain anon/authenticated permissions;
-- it must explicitly SET ROLE per request, which is what PostgREST does.
CREATE ROLE authenticator NOINHERIT LOGIN PASSWORD 'authenticator_password';

-- Allows authenticator to switch into these roles via SET ROLE.
GRANT anon          TO authenticator;
GRANT authenticated TO authenticator;

GRANT USAGE ON SCHEMA public TO anon, authenticated;
GRANT USAGE ON SCHEMA auth   TO anon, authenticated;

-- Only set_auth_context needs to be reachable by API roles.
GRANT EXECUTE ON FUNCTION auth.set_auth_context() TO anon, authenticated;

-- Prevent API roles from reading dbmate's migration history.
REVOKE ALL PRIVILEGES ON TABLE public.schema_migrations FROM anon, authenticated;

-- JWT signing secret. Must match PGRST_JWT_SECRET in the PostgREST config.
-- format() is required because ALTER DATABASE does not accept bind parameters.
DO $$
BEGIN
  EXECUTE format(
    'ALTER DATABASE %I SET "app.jwt_secret" TO %L',
    current_database(),
    'super-secret-jwt-token-with-at-least-32-characters'
  );
END;
$$;

-- migrate:down
DO $$
BEGIN
  EXECUTE format('ALTER DATABASE %I RESET "app.jwt_secret"', current_database());
END;
$$;
DROP ROLE IF EXISTS authenticator;
DROP ROLE IF EXISTS authenticated;
DROP ROLE IF EXISTS anon;
DROP EXTENSION IF EXISTS pgjwt;
DROP SCHEMA IF EXISTS auth CASCADE;
DROP EXTENSION IF EXISTS pgcrypto;
COMMENT ON SCHEMA public IS NULL;

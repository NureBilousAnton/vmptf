-- migrate:up

-- Exposes the authenticated user's own profile without password_hash.
CREATE VIEW me AS SELECT id, email, first_name, last_name, phone, created_at, updated_at FROM users;

-- Execute with the caller's privileges, not view owner. Needed for RLS to return a single user.
ALTER VIEW me SET (security_invoker = true);

COMMENT ON VIEW me IS 'Profile of the currently authenticated user.';

GRANT SELECT ON me TO authenticated;

-- migrate:down
DROP VIEW IF EXISTS me;

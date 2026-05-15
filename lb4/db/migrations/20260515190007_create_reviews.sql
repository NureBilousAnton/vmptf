-- migrate:up
CREATE TABLE reviews (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL DEFAULT current_setting('auth.user_id', true)::uuid REFERENCES users(id) ON DELETE CASCADE,
    product_id  UUID        NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    rating      SMALLINT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
    body        TEXT        CHECK (char_length(body) <= 2000),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    UNIQUE (user_id, product_id)
);

COMMENT ON TABLE  reviews        IS 'Customer reviews for products.';
COMMENT ON COLUMN reviews.rating IS 'Star rating from 1 (worst) to 5 (best).';
COMMENT ON COLUMN reviews.body   IS 'Optional written review text.';

-- Speeds up fetching all reviews for a given product.
CREATE INDEX idx_reviews_product_id ON reviews(product_id);
-- Speeds up fetching all reviews written by a given user.
CREATE INDEX idx_reviews_user_id    ON reviews(user_id);

GRANT SELECT ON reviews TO anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON reviews TO authenticated;

ALTER TABLE reviews ENABLE ROW LEVEL SECURITY;

-- Reviews are readable by everyone.
CREATE POLICY reviews_read ON reviews FOR SELECT TO anon, authenticated USING (true);

-- Only the author can mutate their own reviews.
CREATE POLICY reviews_own ON reviews
  FOR ALL TO authenticated
  USING      (user_id = current_setting('auth.user_id', true)::uuid)
  WITH CHECK (user_id = current_setting('auth.user_id', true)::uuid);

-- migrate:down
DROP TABLE IF EXISTS reviews;

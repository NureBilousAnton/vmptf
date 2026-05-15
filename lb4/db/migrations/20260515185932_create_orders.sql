-- migrate:up
CREATE TYPE order_status AS ENUM ('pending', 'paid', 'shipped', 'delivered', 'cancelled');

CREATE TABLE orders (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    -- DEFAULT fills user_id from the JWT so the client never has to send it.
    user_id     UUID          NOT NULL DEFAULT current_setting('auth.user_id', true)::uuid REFERENCES users(id) ON DELETE RESTRICT,
    status      order_status  NOT NULL DEFAULT 'pending',
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

COMMENT ON TABLE orders IS 'Customer orders. Total is computed as SUM(quantity * unit_price) from order_products.';

CREATE TRIGGER set_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION auth.set_updated_at();

-- Speeds up fetching all orders for a given user.
CREATE INDEX idx_orders_user_id ON orders(user_id);
-- Speeds up filtering orders by status (e.g. all pending orders).
CREATE INDEX idx_orders_status  ON orders(status);

CREATE TABLE order_products (
    order_id    UUID            NOT NULL REFERENCES orders(id)   ON DELETE CASCADE,
    product_id  UUID            NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity    INTEGER         NOT NULL CHECK (quantity > 0),
    unit_price  NUMERIC(10, 2)  NOT NULL CHECK (unit_price >= 0),

    PRIMARY KEY (order_id, product_id)
);

COMMENT ON TABLE  order_products            IS 'Line items belonging to an order.';
COMMENT ON COLUMN order_products.unit_price IS 'Product unit price snapshot at the time of purchase.';

-- Speeds up fetching all line items for a given order.
CREATE INDEX idx_order_products_order_id   ON order_products(order_id);
-- Speeds up finding which orders contain a given product.
CREATE INDEX idx_order_products_product_id ON order_products(product_id);

GRANT SELECT, INSERT, UPDATE ON orders         TO authenticated;
GRANT SELECT, INSERT, DELETE ON order_products TO authenticated;

ALTER TABLE orders         ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_products ENABLE ROW LEVEL SECURITY;

CREATE POLICY orders_own ON orders
  FOR ALL TO authenticated
  USING      (user_id = current_setting('auth.user_id', true)::uuid)
  WITH CHECK (user_id = current_setting('auth.user_id', true)::uuid);

-- order_products has no user_id, so ownership is checked indirectly through the parent order.
CREATE POLICY order_products_own ON order_products
  FOR ALL TO authenticated
  USING (
    EXISTS (SELECT 1 FROM orders
            WHERE orders.id = order_products.order_id
              AND orders.user_id = current_setting('auth.user_id', true)::uuid)
  )
  WITH CHECK (
    EXISTS (SELECT 1 FROM orders
            WHERE orders.id = order_products.order_id
              AND orders.user_id = current_setting('auth.user_id', true)::uuid)
  );

-- migrate:down
DROP TABLE IF EXISTS order_products;
DROP TABLE IF EXISTS orders;
DROP TYPE  IF EXISTS order_status;

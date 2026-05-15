-- migrate:up
CREATE TABLE products (
    id           UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(255)    NOT NULL,
    description  TEXT            CHECK (char_length(description) <= 3000),
    price        NUMERIC(10, 2)  NOT NULL CHECK (price >= 0),
    stock_qty    INTEGER         NOT NULL DEFAULT 0 CHECK (stock_qty >= 0),
    is_active    BOOLEAN         NOT NULL DEFAULT true,
    created_at   TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ     NOT NULL DEFAULT now()
);

COMMENT ON TABLE  products           IS 'Product catalogue.';
COMMENT ON COLUMN products.price     IS 'Current listed price in UAH.';
COMMENT ON COLUMN products.stock_qty IS 'Units available in stock. 0 means out of stock.';
COMMENT ON COLUMN products.is_active IS 'When false the product is hidden from the catalogue (soft delete).';

CREATE TRIGGER set_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION auth.set_updated_at();

-- Speeds up catalogue listings that filter by active status.
CREATE INDEX idx_products_active ON products(created_at DESC) WHERE is_active = true;

GRANT SELECT ON products TO anon, authenticated;

-- migrate:down
DROP TABLE IF EXISTS products;

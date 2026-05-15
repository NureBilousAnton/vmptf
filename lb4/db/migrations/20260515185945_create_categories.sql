-- migrate:up
CREATE TABLE categories (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100)  NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

COMMENT ON TABLE categories IS 'Product categories.';

CREATE TABLE product_categories (
    product_id  UUID NOT NULL REFERENCES products(id)   ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,

    PRIMARY KEY (product_id, category_id)
);

COMMENT ON TABLE product_categories IS 'Many-to-many assignment of products to categories.';

-- Speeds up fetching all products in a given category.
CREATE INDEX idx_product_categories_category_id ON product_categories(category_id);

GRANT SELECT ON categories, product_categories TO anon, authenticated;

-- migrate:down
DROP TABLE IF EXISTS product_categories;
DROP TABLE IF EXISTS categories;

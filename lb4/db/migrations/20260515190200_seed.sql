-- migrate:up
DO $$
DECLARE
  alice_id    uuid;
  bob_id      uuid;
  cat_elec    uuid;
  cat_phones  uuid;
  cat_laptops uuid;
  prod_iphone uuid;
  prod_mbp    uuid;
  prod_pixel  uuid;
  order_id    uuid;
BEGIN

  -- Users (password is 'password123' for both)
  INSERT INTO users (email, password_hash, first_name, last_name, phone)
  VALUES ('alice@example.com', crypt('password123', gen_salt('bf')), 'Alice', 'Smith',  '+380501112233')
  RETURNING id INTO alice_id;

  INSERT INTO users (email, password_hash, first_name, last_name)
  VALUES ('bob@example.com', crypt('password123', gen_salt('bf')), 'Bob', 'Jones')
  RETURNING id INTO bob_id;

  -- Categories
  INSERT INTO categories (name) VALUES ('Electronics')  RETURNING id INTO cat_elec;
  INSERT INTO categories (name) VALUES ('Smartphones')  RETURNING id INTO cat_phones;
  INSERT INTO categories (name) VALUES ('Laptops')      RETURNING id INTO cat_laptops;

  -- Products
  INSERT INTO products (name, description, price, stock_qty)
  VALUES ('iPhone 16 Pro', 'Apple iPhone 16 Pro 256GB', 54999.00, 12)
  RETURNING id INTO prod_iphone;

  INSERT INTO products (name, description, price, stock_qty)
  VALUES ('MacBook Pro 14"', 'Apple MacBook Pro 14-inch M4 Pro 24GB', 89999.00, 5)
  RETURNING id INTO prod_mbp;

  INSERT INTO products (name, description, price, stock_qty)
  VALUES ('Pixel 9', 'Google Pixel 9 128GB', 32999.00, 20)
  RETURNING id INTO prod_pixel;

  -- Product to category assignments
  INSERT INTO product_categories (product_id, category_id) VALUES
    (prod_iphone, cat_elec),
    (prod_iphone, cat_phones),
    (prod_mbp,    cat_elec),
    (prod_mbp,    cat_laptops),
    (prod_pixel,  cat_elec),
    (prod_pixel,  cat_phones);

  -- Alice's order
  INSERT INTO orders (user_id) VALUES (alice_id) RETURNING id INTO order_id;

  INSERT INTO order_products (order_id, product_id, quantity, unit_price) VALUES
    (order_id, prod_iphone, 1, 54999.00),
    (order_id, prod_mbp,    1, 89999.00);

  UPDATE orders SET status = 'paid' WHERE id = order_id;

  -- Reviews
  INSERT INTO reviews (user_id, product_id, rating, body) VALUES
    (alice_id, prod_iphone, 5, 'Excellent phone, very fast and great camera.'),
    (alice_id, prod_mbp,    5, 'Best laptop I have ever owned.'),
    (bob_id,   prod_pixel,  4, 'Great Android experience, solid build quality.');

END;
$$;

-- migrate:down
DELETE FROM users WHERE email IN ('alice@example.com', 'bob@example.com');

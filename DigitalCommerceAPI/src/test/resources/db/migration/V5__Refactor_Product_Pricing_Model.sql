
ALTER TABLE products
    DROP COLUMN IF EXISTS discount,
    ADD COLUMN special_price_active BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE products
    ALTER COLUMN price TYPE NUMERIC(10, 2),
    ALTER COLUMN special_price TYPE NUMERIC(10, 2);

-- 2. Altera a tabela 'carts' para usar NUMERIC para o preço total.
ALTER TABLE carts
    ALTER COLUMN total_price TYPE NUMERIC(10, 2);

-- 3. Altera a tabela 'cart_items' para usar NUMERIC para preços e descontos.
ALTER TABLE cart_items
    ALTER COLUMN discount TYPE NUMERIC(10, 2),
    ALTER COLUMN product_price TYPE NUMERIC(10, 2);

-- 4. Altera a tabela 'order_items' para usar NUMERIC para consistência.
ALTER TABLE order_items
    ALTER COLUMN discount TYPE NUMERIC(10, 2),
    ALTER COLUMN ordered_product_price TYPE NUMERIC(10, 2);
-- 1. Roles (primeiro, pois é referenciada)
CREATE TABLE roles (
    role_id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(20) NOT NULL UNIQUE
);

-- 2. Users
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL
);

-- 3. User_Role (ManyToMany)
CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);

-- 4. Categories
CREATE TABLE categories (
    category_id BIGSERIAL PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE
);

-- 5. Products
CREATE TABLE products (
    product_id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    image TEXT,
    description TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    discount DOUBLE PRECISION DEFAULT 0.0,
    special_price DOUBLE PRECISION,
    category_id BIGINT,
    seller_id BIGINT,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL,
    CONSTRAINT fk_product_seller FOREIGN KEY (seller_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- 6. Addresses
CREATE TABLE addresses (
    address_id BIGSERIAL PRIMARY KEY,
    street VARCHAR(255) NOT NULL,
    building_name VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    cep VARCHAR(20) NOT NULL,
    user_id BIGINT,
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 7. Carts
CREATE TABLE carts (
    cart_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE,
    total_price DOUBLE PRECISION DEFAULT 0.0,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 8. Cart Items
CREATE TABLE cart_items (
    cart_item_id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    discount DOUBLE PRECISION DEFAULT 0.0,
    product_price DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_cartitem_cart FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_cartitem_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- 9. Payments
CREATE TABLE payments (
    payment_id BIGSERIAL PRIMARY KEY,
    payment_method VARCHAR(50) NOT NULL,
    pg_payment_id VARCHAR(255),
    pg_status VARCHAR(50),
    pg_status_detail VARCHAR(100),
    pg_response_message TEXT,
    pg_name VARCHAR(100),
    amount NUMERIC(19,2),
    transaction_id VARCHAR(255),
    qr_code TEXT,
    qr_code_base64 TEXT,
    confirmed_at TIMESTAMP
);

-- 10. Orders
CREATE TABLE orders (
    order_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(50) NOT NULL,
    order_date DATE,
    payment_id BIGINT UNIQUE,
    total_amount DOUBLE PRECISION,
    order_status VARCHAR(50),
    address_id BIGINT,
    CONSTRAINT fk_order_payment FOREIGN KEY (payment_id) REFERENCES payments(payment_id) ON DELETE SET NULL,
    CONSTRAINT fk_order_address FOREIGN KEY (address_id) REFERENCES addresses(address_id) ON DELETE SET NULL
);

-- 11. Order Items
CREATE TABLE order_items (
    order_item_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    discount DOUBLE PRECISION DEFAULT 0.0,
    ordered_product_price DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_orderitem_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    CONSTRAINT fk_orderitem_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);


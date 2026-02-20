-- Create schema for ecommerce database

-- Orders Table
CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(100) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status)
);

-- Products Table
CREATE TABLE IF NOT EXISTS products (
    product_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_stock (stock_quantity)
);

-- Payments Table
CREATE TABLE IF NOT EXISTS payments (
    payment_id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    INDEX idx_order_id (order_id),
    INDEX idx_status (status)
);

-- Insert sample products
INSERT INTO products (product_id, name, description, price, stock_quantity, created_at) VALUES
('prod-001', 'Laptop', 'High-performance laptop', 999.99, 50, CURRENT_TIMESTAMP),
('prod-002', 'Mouse', 'Wireless mouse', 29.99, 200, CURRENT_TIMESTAMP),
('prod-003', 'Keyboard', 'Mechanical keyboard', 79.99, 150, CURRENT_TIMESTAMP),
('prod-004', 'Monitor', '27-inch 4K monitor', 399.99, 30, CURRENT_TIMESTAMP),
('prod-005', 'Headphones', 'Noise-cancelling headphones', 199.99, 100, CURRENT_TIMESTAMP);

-- Create indexes for better query performance
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_created ON orders(created_at);
CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_products_name ON products(name);

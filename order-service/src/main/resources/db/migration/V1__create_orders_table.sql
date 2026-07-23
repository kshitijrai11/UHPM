-- =============================================
-- UltraHPM :: Order Service
-- Flyway Migration V1 — Initial Schema
-- =============================================

CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT         NOT NULL,
    status          VARCHAR(50)    NOT NULL DEFAULT 'CREATED',
    total_amount    NUMERIC(12, 2) NOT NULL,
    currency        VARCHAR(3)     NOT NULL DEFAULT 'USD',
    saga_id         VARCHAR(36),
    shipping_address TEXT,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT         NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id  BIGINT         NOT NULL,
    product_name VARCHAR(255)  NOT NULL,
    quantity    INTEGER        NOT NULL,
    unit_price  NUMERIC(10, 2) NOT NULL
);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_status  ON orders (status);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);

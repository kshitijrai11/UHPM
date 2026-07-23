-- =============================================
-- UltraHPM :: Product Service
-- Flyway Migration V1 — Initial Schema
-- =============================================

CREATE TABLE products (
    id          VARCHAR(36) PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    category    VARCHAR(255)   NOT NULL,
    price       NUMERIC(10, 2) NOT NULL,
    stock_quantity INTEGER     NOT NULL,
    description VARCHAR(2000),
    image_url   VARCHAR(512),
    active      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_category ON products (category);
CREATE INDEX idx_product_status   ON products (active);

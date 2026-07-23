-- =============================================
-- UltraHPM :: Payment Service
-- Flyway Migration V1 — Initial Schema
-- =============================================

CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT         NOT NULL,
    user_id         BIGINT         NOT NULL,
    amount          NUMERIC(12, 2) NOT NULL,
    currency        VARCHAR(3)     NOT NULL DEFAULT 'USD',
    status          VARCHAR(50)    NOT NULL DEFAULT 'INITIATED',
    gateway         VARCHAR(50)    NOT NULL DEFAULT 'STRIPE',
    transaction_ref VARCHAR(255),
    saga_id         VARCHAR(36),
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_payments_user_id  ON payments (user_id);
CREATE INDEX idx_payments_status   ON payments (status);

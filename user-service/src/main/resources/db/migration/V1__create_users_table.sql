-- =============================================
-- UltraHPM :: User Service
-- Flyway Migration V1 — Initial Schema
-- =============================================

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    username    VARCHAR(100) NOT NULL UNIQUE,
    first_name  VARCHAR(100),
    last_name   VARCHAR(100),
    phone       VARCHAR(20),
    keycloak_id VARCHAR(36),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE user_addresses (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label       VARCHAR(50)  NOT NULL DEFAULT 'HOME',
    street      VARCHAR(255) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    state       VARCHAR(100),
    zip_code    VARCHAR(20)  NOT NULL,
    country     VARCHAR(100) NOT NULL DEFAULT 'US',
    is_default  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email      ON users (email);
CREATE INDEX idx_users_keycloak   ON users (keycloak_id);
CREATE INDEX idx_addresses_user   ON user_addresses (user_id);

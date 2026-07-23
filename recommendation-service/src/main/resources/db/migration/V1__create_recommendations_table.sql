-- =============================================
-- UltraHPM :: Recommendation Service
-- Flyway Migration V1 — Initial Schema (R2DBC compatible)
-- =============================================

CREATE TABLE user_interactions (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    product_id  BIGINT      NOT NULL,
    event_type  VARCHAR(50) NOT NULL,
    session_id  VARCHAR(100),
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE recommendation_cache (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT    NOT NULL UNIQUE,
    recommended_ids BIGINT[]  NOT NULL,
    model_version   VARCHAR(50),
    generated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_interactions_user    ON user_interactions (user_id);
CREATE INDEX idx_interactions_product ON user_interactions (product_id);
CREATE INDEX idx_rec_cache_user       ON recommendation_cache (user_id);

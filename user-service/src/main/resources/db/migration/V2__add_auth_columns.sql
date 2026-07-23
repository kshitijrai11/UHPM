-- =============================================
-- UltraHPM :: User Service
-- Flyway Migration V2 — Add auth columns
-- =============================================

ALTER TABLE users ADD COLUMN password_hash VARCHAR(255);
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER';

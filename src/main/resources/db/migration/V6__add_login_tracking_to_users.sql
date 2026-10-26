-- ==============================================================================
-- MedScan AI - Migración para seguimiento de usuarios logeados (Flyway V6)
-- ==============================================================================

ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_logged_in BOOLEAN DEFAULT FALSE;

-- ==============================================================================
-- MedScan AI - Migración para agregar fecha de registro a usuarios (Flyway V4)
-- ==============================================================================

ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP;

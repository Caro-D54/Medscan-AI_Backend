-- ==============================================================================
-- MedScan AI - Migración para campos de posología de medicamentos (Flyway V3)
-- ==============================================================================

ALTER TABLE medicaments ADD COLUMN IF NOT EXISTS dosage VARCHAR(255);
ALTER TABLE medicaments ADD COLUMN IF NOT EXISTS frequency VARCHAR(255);
ALTER TABLE medicaments ADD COLUMN IF NOT EXISTS instructions VARCHAR(1000);

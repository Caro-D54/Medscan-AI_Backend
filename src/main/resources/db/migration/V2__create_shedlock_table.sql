-- ==============================================================================
-- MedScan AI - Migración Flyway V2: Tabla para Bloqueos Distribuidos (ShedLock)
-- Cumplimiento 12-Factor App: Factor VI (Processes) y Factor VIII (Concurrency)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    locked_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);

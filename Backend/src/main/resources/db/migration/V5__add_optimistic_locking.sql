-- Agregar columna version para bloqueo optimista JPA en zonas_almacenaje
ALTER TABLE zonas_almacenaje ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Agregar columna version para bloqueo optimista JPA en paquetes (si es necesario)
ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

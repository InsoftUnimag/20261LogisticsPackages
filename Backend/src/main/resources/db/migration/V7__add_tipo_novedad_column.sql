-- Agregar columna tipo_novedad a historial_estados para preservar contexto de novedades
ALTER TABLE historial_estados ADD COLUMN IF NOT EXISTS tipo_novedad VARCHAR(50);
COMMENT ON COLUMN historial_estados.tipo_novedad IS 'Tipo de novedad (DAÑADO, EXTRAVIADO, etc.) preservado en el historial';

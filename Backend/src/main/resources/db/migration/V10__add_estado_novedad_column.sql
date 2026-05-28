-- V10__add_estado_novedad_column.sql
-- Agrega columna para persistir el estado de la novedad (PENDIENTE, NOTIFICADO, CERRADO)
-- Permite que el botón "Cerrar Novedad" guarde su estado correctamente

ALTER TABLE historial_estados
ADD COLUMN estado_novedad VARCHAR(50) DEFAULT 'PENDIENTE'
    CONSTRAINT chk_estado_novedad CHECK (estado_novedad IN ('PENDIENTE', 'NOTIFICADO', 'CERRADO'));

-- Comentario explicativo
COMMENT ON COLUMN historial_estados.estado_novedad IS 'Estado de la novedad: PENDIENTE (inicial), NOTIFICADO (cliente notificado), CERRADO (novedad cerrada)';

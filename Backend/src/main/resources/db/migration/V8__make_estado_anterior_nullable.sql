-- Migración V8: Hacer nullable la columna estado_anterior en historial_estados
-- Justificación: El primer registro de historial (transición a RECIBIDO_EN_SEDE) no tiene estado anterior
-- Esta columna permite null para distinguir el evento inicial de otras transiciones

ALTER TABLE historial_estados 
ALTER COLUMN estado_anterior DROP NOT NULL;

-- Comentario actualizado
COMMENT ON COLUMN historial_estados.estado_anterior IS 'Estado del paquete antes de la transición. NULL para la primera transición (admisión)';

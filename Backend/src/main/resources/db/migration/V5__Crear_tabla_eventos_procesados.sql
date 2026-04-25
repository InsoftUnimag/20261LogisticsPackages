-- V5: Creación de tabla para eventos procesados (Idempotencia)
-- MOD1-UC-007: FR-008 - Detectar y prevenir el procesamiento de eventos duplicados

-- Tabla para almacenar eventos procesados
CREATE TABLE eventos_procesados (
    evento_id VARCHAR(255) PRIMARY KEY,
    paquete_id UUID NOT NULL,
    tipo_evento VARCHAR(100) NOT NULL,
    fecha_procesamiento_utc TIMESTAMP NOT NULL
);

-- Índice para búsquedas rápidas por paquete
CREATE INDEX idx_eventos_procesados_paquete_id ON eventos_procesados(paquete_id);

-- Índice para búsquedas por fecha de procesamiento
CREATE INDEX idx_eventos_procesados_fecha ON eventos_procesados(fecha_procesamiento_utc);

-- Comentarios de las columnas
COMMENT ON TABLE eventos_procesados IS 'Tabla para registro de eventos ya procesados, garantizando idempotencia';
COMMENT ON COLUMN eventos_procesados.evento_id IS 'ID único del evento proveniente del Módulo 2';
COMMENT ON COLUMN eventos_procesados.paquete_id IS 'ID del paquete asociado al evento';
COMMENT ON COLUMN eventos_procesados.tipo_evento IS 'Tipo de evento procesado (EN_TRANSITO, ENTREGADO, etc.)';
COMMENT ON COLUMN eventos_procesados.fecha_procesamiento_utc IS 'Timestamp UTC del momento en que se procesó el evento';

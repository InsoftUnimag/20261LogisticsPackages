-- MOD1-UC-006: Crear tabla para el historial inmutable de estados de paquetes
-- FR-001, FR-002, FR-003: Historial cronológico con fecha/hora, usuario y evidencia

CREATE TABLE IF NOT EXISTS historial_estados (
    id UUID PRIMARY KEY,
    paquete_id UUID NOT NULL,
    estado_anterior VARCHAR(50) NOT NULL,
    estado_nuevo VARCHAR(50) NOT NULL,
    observaciones TEXT,
    usuario_id UUID NOT NULL,
    url_evidencia VARCHAR(500),
    fecha_transicion_utc TIMESTAMP NOT NULL,
    
    -- Foreign key al paquete
    CONSTRAINT fk_historial_paquete 
        FOREIGN KEY (paquete_id) 
        REFERENCES paquetes(id)
        ON DELETE CASCADE
);

-- Índice para consultas frecuentes por paquete ordenadas por fecha
CREATE INDEX idx_historial_paquete_fecha 
    ON historial_estados(paquete_id, fecha_transicion_utc);

-- Índice para consultas por usuario (auditoría)
CREATE INDEX idx_historial_usuario 
    ON historial_estados(usuario_id);

-- Comentarios para documentación
COMMENT ON TABLE historial_estados IS 'Registro inmutable del historial de transiciones de estado de paquetes';
COMMENT ON COLUMN historial_estados.paquete_id IS 'ID del paquete al que pertenece este registro';
COMMENT ON COLUMN historial_estados.estado_anterior IS 'Estado del paquete antes de la transición';
COMMENT ON COLUMN historial_estados.estado_nuevo IS 'Estado del paquete después de la transición';
COMMENT ON COLUMN historial_estados.observaciones IS 'Notas descriptivas sobre la transición';
COMMENT ON COLUMN historial_estados.usuario_id IS 'ID del usuario responsable de la transición';
COMMENT ON COLUMN historial_estados.url_evidencia IS 'URL de la evidencia multimedia (obligatoria para novedades tipo DAÑADO)';
COMMENT ON COLUMN historial_estados.fecha_transicion_utc IS 'Timestamp UTC del momento de la transición';

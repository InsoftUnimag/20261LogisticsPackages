-- V4: MOD1-IP-006: Actualizar Estado de Paquete por Novedad
-- Migración para soportar historial inmutable de estados y novedades en bodega

-- Agregar nuevos estados al enum estado_paquete
ALTER TYPE estado_paquete_enum ADD VALUE IF NOT EXISTS 'NOVEDAD_EN_BODEGA';

-- Crear tipo enum para tipo de novedad
CREATE TYPE tipo_novedad_enum AS ENUM (
    'DAÑADO',
    'EXTRAVIADO'
);

-- Crear tipo enum para tipo de archivo de evidencia
CREATE TYPE tipo_archivo_enum AS ENUM (
    'FOTO',
    'VIDEO'
);

-- Tabla de historial de estados (inmutable, solo INSERT)
CREATE TABLE historial_estados (
    id UUID PRIMARY KEY,
    paquete_id UUID NOT NULL,
    estado_anterior estado_paquete_enum,
    estado_nuevo estado_paquete_enum NOT NULL,
    fecha_transicion TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    usuario_responsable VARCHAR(255) NOT NULL,
    notas TEXT,
    
    -- Auditoría
    created_at TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    
    -- Foreign Key
    CONSTRAINT fk_historial_paquete
        FOREIGN KEY(paquete_id)
        REFERENCES paquetes(id)
        ON DELETE CASCADE,
    
    -- Constraint: No permitir modificaciones posteriores
    CONSTRAINT chk_fecha_transicion_no_futuro 
        CHECK (fecha_transicion <= (NOW() AT TIME ZONE 'UTC'))
);

-- Tabla de evidencias multimedia
CREATE TABLE evidencias (
    id UUID PRIMARY KEY,
    historial_estado_id UUID NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    url_almacenamiento VARCHAR(500) NOT NULL,
    tipo_archivo tipo_archivo_enum NOT NULL,
    tamanio_bytes BIGINT NOT NULL,
    fecha_captura TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    
    -- Foreign Key
    CONSTRAINT fk_evidencia_historial
        FOREIGN KEY(historial_estado_id)
        REFERENCES historial_estados(id)
        ON DELETE CASCADE,
    
    -- Constraints de validación
    CONSTRAINT chk_tamanio_positivo 
        CHECK (tamanio_bytes > 0),
    CONSTRAINT chk_url_no_vacia 
        CHECK (LENGTH(TRIM(url_almacenamiento)) > 0)
);

-- Tabla de novedades en bodega
CREATE TABLE novedades_bodega (
    id UUID PRIMARY KEY,
    paquete_id UUID NOT NULL,
    tipo tipo_novedad_enum NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_deteccion TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    almacenista_responsable VARCHAR(255) NOT NULL,
    
    -- Auditoría
    created_at TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    
    -- Foreign Key
    CONSTRAINT fk_novedad_paquete
        FOREIGN KEY(paquete_id)
        REFERENCES paquetes(id)
        ON DELETE CASCADE,
    
    -- Constraint: descripción no vacía
    CONSTRAINT chk_descripcion_no_vacia 
        CHECK (LENGTH(TRIM(descripcion)) > 0)
);

-- Tabla de relación entre novedades y evidencias
CREATE TABLE novedades_evidencias (
    novedad_id UUID NOT NULL,
    evidencia_id UUID NOT NULL,
    
    PRIMARY KEY (novedad_id, evidencia_id),
    
    CONSTRAINT fk_novedad_evidencia_novedad
        FOREIGN KEY(novedad_id)
        REFERENCES novedades_bodega(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_novedad_evidencia_evidencia
        FOREIGN KEY(evidencia_id)
        REFERENCES evidencias(id)
        ON DELETE CASCADE
);

-- Índices para optimizar consultas
CREATE INDEX idx_historial_paquete_id ON historial_estados(paquete_id);
CREATE INDEX idx_historial_fecha_transicion ON historial_estados(fecha_transicion DESC);
CREATE INDEX idx_historial_usuario ON historial_estados(usuario_responsable);

CREATE INDEX idx_evidencias_historial ON evidencias(historial_estado_id);
CREATE INDEX idx_evidencias_tipo ON evidencias(tipo_archivo);

CREATE INDEX idx_novedades_paquete ON novedades_bodega(paquete_id);
CREATE INDEX idx_novedades_tipo ON novedades_bodega(tipo);
CREATE INDEX idx_novedades_fecha ON novedades_bodega(fecha_deteccion DESC);

-- Comentarios para documentación
COMMENT ON TABLE historial_estados IS 'Historial inmutable de transiciones de estado de paquetes (append-only)';
COMMENT ON TABLE novedades_bodega IS 'Registro de novedades detectadas en bodega (paquetes dañados o extraviados)';
COMMENT ON TABLE evidencias IS 'Archivos multimedia adjuntos como evidencia de novedades';

COMMENT ON COLUMN historial_estados.fecha_transicion IS 'Timestamp UTC de la transición de estado';
COMMENT ON COLUMN historial_estados.usuario_responsable IS 'Identificador del usuario que realizó el cambio';
COMMENT ON COLUMN novedades_bodega.tipo IS 'Tipo de novedad: DAÑADO o EXTRAVIADO';

-- Configuración de seguridad: Bloquear modificaciones directas al historial
-- (Solo se permiten INSERT, no UPDATE ni DELETE directo)
REVOKE UPDATE, DELETE ON historial_estados FROM PUBLIC;
GRANT SELECT, INSERT ON historial_estados TO PUBLIC;

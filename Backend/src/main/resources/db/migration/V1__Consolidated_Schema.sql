-- Esquema Consolidado de Base de Datos - Estado Final
-- Generado: 2026-04-29
-- Este archivo representa el estado final después de aplicar todas las migraciones

-- ============================================
-- ENUM TYPES (V1 + V6)
-- ============================================

CREATE TYPE estado_paquete_enum AS ENUM (
    'RECIBIDO_EN_SEDE',
    'EN_CLASIFICACION',
    'LISTO_PARA_DESPACHO',
    'EN_TRANSITO',
    'ENTREGADO',
    'NOVEDAD_EN_BODEGA',
    'EN_PARADA_DE_ENTREGA',
    'DEVOLUCION_EN_RUTA',
    'EXTRAVIADO_EN_RUTA',
    'DAÑADO_EN_RUTA'
);

CREATE TYPE estado_gps_enum AS ENUM (
    'PENDIENTE',
    'RESUELTO'
);

CREATE TYPE metodo_pago_enum AS ENUM (
    'PREPAGO',
    'CONTRA_ENTREGA'
);

CREATE TYPE tipo_mercancia_enum AS ENUM (
    'ESTANDAR',
    'FRAGIL',
    'PELIGROSO'
);

CREATE TYPE categoria_carga_enum AS ENUM (
    'NORMAL',
    'CARGA_ESPECIAL'
);

CREATE TYPE tipo_documento_enum AS ENUM (
    'CEDULA_CIUDADANIA',
    'CEDULA_EXTRANJERIA',
    'PASAPORTE',
    'NIT'
);

CREATE TYPE categoria_zona_enum AS ENUM (
    'NORMAL',
    'DELICADA',
    'ALTO_RIESGO',
    'RETENCION'
);

CREATE TYPE estado_zona_enum AS ENUM (
    'DISPONIBLE',
    'PARCIAL',
    'SATURADO',
    'BLOQUEADO'
);

CREATE TYPE tipo_sede_enum AS ENUM (
    'PRINCIPAL',
    'AUXILIAR'
);

-- ============================================
-- TABLES (Estado Final)
-- ============================================

-- Tabla para almacenar información de personas (remitentes y destinatarios)
CREATE TABLE personas (
    id UUID PRIMARY KEY,
    tipo_documento tipo_documento_enum,
    numero_documento VARCHAR(255) UNIQUE,
    nombre_completo VARCHAR(255),
    telefono VARCHAR(255),
    correo_electronico VARCHAR(255),
    direccion VARCHAR(255)
);

-- Tabla de sedes (V8)
CREATE TABLE sedes (
    id UUID PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    direccion VARCHAR(500),
    ciudad VARCHAR(255),
    departamento VARCHAR(255),
    pais VARCHAR(255),
    tipo tipo_sede_enum,
    capacidad_maxima_peso NUMERIC(10, 2),
    capacidad_maxima_volumen NUMERIC(10, 3),
    tarifa_base NUMERIC(10, 2),
    tarifa_por_kg NUMERIC(10, 2),
    tarifa_por_km NUMERIC(10, 2),
    metodos_pago_habilitados TEXT[],
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla para zonas de almacenaje (V2, con id_sede UUID de V8)
CREATE TABLE zonas_almacenaje (
    id UUID PRIMARY KEY,
    nombre VARCHAR(255),
    codigo VARCHAR(255) UNIQUE,
    categoria categoria_zona_enum,
    capacidad_max_kg NUMERIC(10, 2),
    capacidad_max_m3 NUMERIC(10, 3),
    capacidad_max_paquetes INTEGER,
    peso_actual_kg NUMERIC(10, 2),
    volumen_actual_m3 NUMERIC(10, 3),
    contador_paquetes INTEGER,
    estado estado_zona_enum,
    ubicacion_fisica VARCHAR(255),
    id_sede UUID,
    zona_contingencia_id UUID
);

-- Tabla principal de paquetes (V2 + V6 + V7 + V8)
CREATE TABLE paquetes (
    id UUID PRIMARY KEY,
    fecha_ingreso_utc TIMESTAMP,
    estado estado_paquete_enum,
    sede_id UUID,
    direccion_destino VARCHAR(255),
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION,
    estado_gps estado_gps_enum,
    valor_declarado NUMERIC(19, 2),
    metodo_pago metodo_pago_enum,

    -- Referencias a la tabla personas
    remitente_id UUID,
    destinatario_id UUID,

    peso DOUBLE PRECISION,
    largo DOUBLE PRECISION,
    ancho DOUBLE PRECISION,
    alto DOUBLE PRECISION,
    volumen_m3 DOUBLE PRECISION,
    peso_volumetrico DOUBLE PRECISION,
    peso_facturable DOUBLE PRECISION,
    tipo_mercancia tipo_mercancia_enum,
    categoria_carga categoria_carga_enum,
    indicador_forma_irregular BOOLEAN,
    precio_envio NUMERIC(19, 2),
    distancia_estimada_km DOUBLE PRECISION,
    ruta_id UUID,
    zona_almacenamiento_id UUID,
    zona_destino_id UUID,

    -- Columnas agregadas en V6 (evidencia de entrega)
    url_evidencia_entrega TEXT,
    nombre_firmante VARCHAR(255),
    fecha_entrega_utc TIMESTAMP,

    -- Columnas agregadas en V7 (alertas)
    alerta_carga_especial BOOLEAN DEFAULT FALSE,
    alerta_densidad_atipica BOOLEAN DEFAULT FALSE,
    etiqueta_digital TEXT,

    -- Definición de las claves foráneas
    CONSTRAINT fk_remitente
        FOREIGN KEY(remitente_id)
        REFERENCES personas(id),
    CONSTRAINT fk_destinatario
        FOREIGN KEY(destinatario_id)
        REFERENCES personas(id)
);

-- Tabla de Zonas de Destino (V3)
CREATE TABLE zonas_destino (
    id UUID PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    categoria categoria_zona_enum NOT NULL,

    -- Límites geográficos de la zona
    latitud_min DOUBLE PRECISION,
    latitud_max DOUBLE PRECISION,
    longitud_min DOUBLE PRECISION,
    longitud_max DOUBLE PRECISION,

    -- Capacidad y control
    capacidad_max_paquetes INTEGER,
    contador_paquetes INTEGER DEFAULT 0 NOT NULL,

    -- Relación con sede
    id_sede UUID,

    -- Constraints
    CONSTRAINT chk_latitud_valida CHECK (
        (latitud_min IS NULL AND latitud_max IS NULL) OR
        (latitud_min >= -90 AND latitud_max <= 90 AND latitud_min <= latitud_max)
    ),
    CONSTRAINT chk_longitud_valida CHECK (
        (longitud_min IS NULL AND longitud_max IS NULL) OR
        (longitud_min >= -180 AND longitud_max <= 180 AND longitud_min <= longitud_max)
    ),
    CONSTRAINT chk_contador_no_negativo CHECK (contador_paquetes >= 0)
);

-- Índices para zonas_destino
CREATE INDEX idx_zonas_destino_sede ON zonas_destino(id_sede);
CREATE INDEX idx_zonas_destino_categoria ON zonas_destino(categoria);
CREATE INDEX idx_zonas_destino_capacidad ON zonas_destino(capacidad_max_paquetes, contador_paquetes);
CREATE INDEX idx_zonas_destino_limites ON zonas_destino(latitud_min, latitud_max, longitud_min, longitud_max);

-- Tabla para el historial inmutable de estados de paquetes (V4)
CREATE TABLE historial_estados (
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

-- Índices para historial_estados
CREATE INDEX idx_historial_paquete_fecha ON historial_estados(paquete_id, fecha_transicion_utc);
CREATE INDEX idx_historial_usuario ON historial_estados(usuario_id);

-- Tabla para eventos procesados - Idempotencia (V5)
CREATE TABLE eventos_procesados (
    evento_id VARCHAR(255) PRIMARY KEY,
    paquete_id UUID NOT NULL,
    tipo_evento VARCHAR(100) NOT NULL,
    fecha_procesamiento_utc TIMESTAMP NOT NULL
);

-- Índices para eventos_procesados
CREATE INDEX idx_eventos_procesados_paquete_id ON eventos_procesados(paquete_id);
CREATE INDEX idx_eventos_procesados_fecha ON eventos_procesados(fecha_procesamiento_utc);

-- ============================================
-- COMENTARIOS DE DOCUMENTACIÓN
-- ============================================

COMMENT ON TABLE zonas_destino IS 'Tabla para almacenar zonas de destino lógicas basadas en proximidad geográfica';
COMMENT ON COLUMN zonas_destino.codigo IS 'Código único de la zona (ej: ZD-NORTE-01)';
COMMENT ON COLUMN zonas_destino.categoria IS 'Categoría de zona que determina qué tipos de mercancía puede albergar';
COMMENT ON COLUMN zonas_destino.latitud_min IS 'Límite sur de la zona (mínima latitud)';
COMMENT ON COLUMN zonas_destino.latitud_max IS 'Límite norte de la zona (máxima latitud)';
COMMENT ON COLUMN zonas_destino.longitud_min IS 'Límite oeste de la zona (mínima longitud)';
COMMENT ON COLUMN zonas_destino.longitud_max IS 'Límite este de la zona (máxima longitud)';
COMMENT ON COLUMN zonas_destino.capacidad_max_paquetes IS 'Capacidad máxima de paquetes que puede albergar la zona';
COMMENT ON COLUMN zonas_destino.contador_paquetes IS 'Número actual de paquetes clasificados en esta zona';

COMMENT ON TABLE historial_estados IS 'Registro inmutable del historial de transiciones de estado de paquetes';
COMMENT ON COLUMN historial_estados.paquete_id IS 'ID del paquete al que pertenece este registro';
COMMENT ON COLUMN historial_estados.estado_anterior IS 'Estado del paquete antes de la transición';
COMMENT ON COLUMN historial_estados.estado_nuevo IS 'Estado del paquete después de la transición';
COMMENT ON COLUMN historial_estados.observaciones IS 'Notas descriptivas sobre la transición';
COMMENT ON COLUMN historial_estados.usuario_id IS 'ID del usuario responsable de la transición';
COMMENT ON COLUMN historial_estados.url_evidencia IS 'URL de la evidencia multimedia (obligatoria para novedades tipo DAÑADO)';
COMMENT ON COLUMN historial_estados.fecha_transicion_utc IS 'Timestamp UTC del momento de la transición';

COMMENT ON TABLE eventos_procesados IS 'Tabla para registro de eventos ya procesados, garantizando idempotencia';
COMMENT ON COLUMN eventos_procesados.evento_id IS 'ID único del evento proveniente del Módulo 2';
COMMENT ON COLUMN eventos_procesados.paquete_id IS 'ID del paquete asociado al evento';
COMMENT ON COLUMN eventos_procesados.tipo_evento IS 'Tipo de evento procesado (EN_TRANSITO, ENTREGADO, etc.)';
COMMENT ON COLUMN eventos_procesados.fecha_procesamiento_utc IS 'Timestamp UTC del momento en que se procesó el evento';

COMMENT ON COLUMN paquetes.url_evidencia_entrega IS 'URL de la evidencia de entrega (POD - Proof of Delivery)';
COMMENT ON COLUMN paquetes.nombre_firmante IS 'Nombre de la persona que recibe el paquete';
COMMENT ON COLUMN paquetes.fecha_entrega_utc IS 'Timestamp UTC del momento de entrega del paquete';
COMMENT ON COLUMN paquetes.alerta_carga_especial IS 'Indica si el paquete requiere manejo especial por peso o volumen';
COMMENT ON COLUMN paquetes.alerta_densidad_atipica IS 'Indica si la densidad del paquete es atípica (>30% diferencia)';
COMMENT ON COLUMN paquetes.etiqueta_digital IS 'Etiqueta digital única vinculada al UUID del paquete';

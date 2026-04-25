-- MOD1-IP-005: Clasificar Paquete por Zona de Destino
-- Migración para crear la tabla zonas_destino

-- Tabla de Zonas de Destino (agrupación lógica geográfica)
CREATE TABLE IF NOT EXISTS zonas_destino (
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

-- Índices para mejorar el rendimiento de las consultas
CREATE INDEX idx_zonas_destino_sede ON zonas_destino(id_sede);
CREATE INDEX idx_zonas_destino_categoria ON zonas_destino(categoria);
CREATE INDEX idx_zonas_destino_capacidad ON zonas_destino(capacidad_max_paquetes, contador_paquetes);

-- Índice espacial para búsquedas por coordenadas (opcional, dependiendo del motor de BD)
CREATE INDEX idx_zonas_destino_limites ON zonas_destino(latitud_min, latitud_max, longitud_min, longitud_max);

-- Comentarios para documentación
COMMENT ON TABLE zonas_destino IS 'Tabla para almacenar zonas de destino lógicas basadas en proximidad geográfica';
COMMENT ON COLUMN zonas_destino.codigo IS 'Código único de la zona (ej: ZD-NORTE-01)';
COMMENT ON COLUMN zonas_destino.categoria IS 'Categoría de zona que determina qué tipos de mercancía puede albergar';
COMMENT ON COLUMN zonas_destino.latitud_min IS 'Límite sur de la zona (mínima latitud)';
COMMENT ON COLUMN zonas_destino.latitud_max IS 'Límite norte de la zona (máxima latitud)';
COMMENT ON COLUMN zonas_destino.longitud_min IS 'Límite oeste de la zona (mínima longitud)';
COMMENT ON COLUMN zonas_destino.longitud_max IS 'Límite este de la zona (máxima longitud)';
COMMENT ON COLUMN zonas_destino.capacidad_max_paquetes IS 'Capacidad máxima de paquetes que puede albergar la zona';
COMMENT ON COLUMN zonas_destino.contador_paquetes IS 'Número actual de paquetes clasificados en esta zona';

-- Datos de ejemplo para testing (opcional - comentar en producción)
INSERT INTO zonas_destino (id, nombre, codigo, categoria, latitud_min, latitud_max, longitud_min, longitud_max, capacidad_max_paquetes, contador_paquetes, id_sede)
VALUES 
    (gen_random_uuid(), 'Zona Norte Bogotá', 'ZD-NORTE-01', 'NORMAL', 4.70, 4.80, -74.10, -74.00, 1000, 0, NULL),
    (gen_random_uuid(), 'Zona Sur Bogotá', 'ZD-SUR-01', 'NORMAL', 4.55, 4.65, -74.15, -74.05, 1000, 0, NULL),
    (gen_random_uuid(), 'Zona Centro Bogotá', 'ZD-CENTRO-01', 'DELICADA', 4.65, 4.70, -74.08, -74.03, 500, 0, NULL),
    (gen_random_uuid(), 'Zona Industrial', 'ZD-IND-01', 'ALTO_RIESGO', 4.60, 4.65, -74.20, -74.10, 200, 0, NULL);

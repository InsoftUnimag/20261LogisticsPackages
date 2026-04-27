-- V8: Actualizar sede_id de VARCHAR a UUID y crear tabla de sedes

-- Crear tabla de sedes con los nuevos campos
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

-- Actualizar columna sede_id en tabla paquetes de VARCHAR a UUID
ALTER TABLE paquetes ALTER COLUMN sede_id TYPE UUID USING sede_id::UUID;

-- Añadir nuevas columnas de alertas si no existen
ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS alerta_carga_especial BOOLEAN DEFAULT FALSE;
ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS alerta_densidad_atipica BOOLEAN DEFAULT FALSE;

-- Actualizar columnas de evidencia de entrega si no existen
ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS url_evidencia_entrega TEXT;
ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS nombre_firmante VARCHAR(255);
ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS fecha_entrega_utc TIMESTAMP;
ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS etiqueta_digital TEXT;

-- Insertar sedes de ejemplo
INSERT INTO sedes (id, nombre, direccion, ciudad, departamento, pais, tipo, capacidad_maxima_peso, capacidad_maxima_volumen, tarifa_base, tarifa_por_kg, tarifa_por_km, metodos_pago_habilitados) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'Sede Principal Bogotá', 'Carrera 7 # 71-21', 'Bogotá', 'Cundinamarca', 'Colombia', 'PRINCIPAL', 5000.00, 100.00, 5000.00, 150.00, 80.00, ARRAY['PREPAGO', 'CONTRA_ENTREGA']),
('660e8400-e29b-41d4-a716-446655440001', 'Sede Auxiliar Medellín', 'Calle 50 # 51-25', 'Medellín', 'Antioquia', 'Colombia', 'AUXILIAR', 3000.00, 50.00, 4500.00, 180.00, 95.00, ARRAY['PREPAGO'])
ON CONFLICT (id) DO NOTHING;
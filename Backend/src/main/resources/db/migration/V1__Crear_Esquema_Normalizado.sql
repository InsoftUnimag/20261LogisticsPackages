-- V1: Creación del esquema inicial normalizado

-- Tabla para almacenar información de personas (remitentes y destinatarios)
CREATE TABLE personas (
    id UUID PRIMARY KEY,
    tipo_documento VARCHAR(255),
    numero_documento VARCHAR(255) UNIQUE,
    nombre_completo VARCHAR(255),
    telefono VARCHAR(255),
    correo_electronico VARCHAR(255),
    direccion VARCHAR(255)
);

-- Tabla para zonas de almacenaje
CREATE TABLE zonas_almacenaje (
    id UUID PRIMARY KEY,
    nombre VARCHAR(255),
    codigo VARCHAR(255) UNIQUE,
    categoria VARCHAR(255),
    capacidad_max_kg NUMERIC(10, 2),
    capacidad_max_m3 NUMERIC(10, 3),
    capacidad_max_paquetes INTEGER,
    peso_actual_kg NUMERIC(10, 2),
    volumen_actual_m3 NUMERIC(10, 3),
    contador_paquetes INTEGER,
    estado VARCHAR(255),
    ubicacion_fisica VARCHAR(255),
    id_sede UUID,
    zona_contingencia_id UUID
);

-- Tabla principal de paquetes con referencias a personas
CREATE TABLE paquetes (
    id UUID PRIMARY KEY,
    fecha_ingreso_utc TIMESTAMP,
    estado VARCHAR(255),
    sede_id VARCHAR(255),
    direccion_destino VARCHAR(255),
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION,
    estado_gps VARCHAR(255),
    valor_declarado NUMERIC(19, 2),
    metodo_pago VARCHAR(255),

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
    tipo_mercancia VARCHAR(255),
    categoria_carga VARCHAR(255),
    indicador_forma_irregular BOOLEAN,
    precio_envio NUMERIC(19, 2),
    distancia_estimada_km DOUBLE PRECISION,
    ruta_id UUID,
    zona_almacenamiento_id UUID,
    zona_destino_id UUID,

    -- Definición de las claves foráneas
    CONSTRAINT fk_remitente
        FOREIGN KEY(remitente_id)
        REFERENCES personas(id),
    CONSTRAINT fk_destinatario
        FOREIGN KEY(destinatario_id)
        REFERENCES personas(id)
);

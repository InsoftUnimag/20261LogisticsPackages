-- V1: Creación de los tipos ENUM nativos de PostgreSQL para el dominio

CREATE TYPE estado_paquete_enum AS ENUM (
    'RECIBIDO_EN_SEDE',
    'EN_CLASIFICACION',
    'LISTO_PARA_DESPACHO',
    'EN_TRANSITO',
    'ENTREGADO'
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

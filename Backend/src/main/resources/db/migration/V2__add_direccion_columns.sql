-- V2: Agregar columnas separadas para Direccion embebida
-- Fecha: 2026-05-01

-- Modificar tabla personas - agregar columnas de dirección separadas
ALTER TABLE personas 
ADD COLUMN IF NOT EXISTS ciudad VARCHAR(255),
ADD COLUMN IF NOT EXISTS departamento VARCHAR(255),
ADD COLUMN IF NOT EXISTS pais VARCHAR(255);

-- Renombrar columna existente si es necesario (la migración anterior tenía 'direccion')
ALTER TABLE personas RENAME COLUMN direccion TO direccion_linea;

-- Modificar tabla pacotes - agregar columnas de dirección destino separadas
ALTER TABLE paquetes 
ADD COLUMN IF NOT EXISTS ciudad_destino VARCHAR(255),
ADD COLUMN IF NOT EXISTS departamento_destino VARCHAR(255),
ADD COLUMN IF NOT EXISTS pais_destino VARCHAR(255);

-- Renombrar columna existente
ALTER TABLE paquetes RENAME COLUMN direccion_destino TO direccion_linea;
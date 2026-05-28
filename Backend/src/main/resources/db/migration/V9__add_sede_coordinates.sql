-- Migración V9: Agregar columnas de coordenadas a tabla sedes
-- Fecha: 2026-05-27
-- Descripción: Agrega soporte para coordenadas geográficas (latitud, longitud) en cada sede
-- Estas coordenadas se utilizan en DistanceCalculatorAdapter para calcular distancias reales desde cada sede

ALTER TABLE sedes ADD COLUMN IF NOT EXISTS latitud DOUBLE PRECISION;
ALTER TABLE sedes ADD COLUMN IF NOT EXISTS longitud DOUBLE PRECISION;

-- Nota: Los valores de latitud y longitud ya se insertan en V3__seed_data.sql
-- Esta migración solo asegura que las columnas existen en BD existentes

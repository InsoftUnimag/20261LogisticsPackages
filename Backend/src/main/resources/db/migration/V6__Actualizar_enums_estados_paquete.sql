-- V6: Actualización de enums para incluir nuevos estados de ruta
-- MOD1-UC-007: Agregar estados de ruta al enum estado_paquete_enum

-- Agregar nuevos valores al enum estado_paquete_enum
ALTER TYPE estado_paquete_enum ADD VALUE IF NOT EXISTS 'EN_PARADA_DE_ENTREGA';
ALTER TYPE estado_paquete_enum ADD VALUE IF NOT EXISTS 'DEVOLUCION_EN_RUTA';
ALTER TYPE estado_paquete_enum ADD VALUE IF NOT EXISTS 'EXTRAVIADO_EN_RUTA';
ALTER TYPE estado_paquete_enum ADD VALUE IF NOT EXISTS 'DAÑADO_EN_RUTA';

-- Agregar nuevas columnas a la tabla paquetes para evidencia de entrega
ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS url_evidencia_entrega VARCHAR(500);
ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS nombre_firmante VARCHAR(255);
ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS fecha_entrega_utc TIMESTAMP;

-- Comentarios de las nuevas columnas
COMMENT ON COLUMN paquetes.url_evidencia_entrega IS 'URL de la evidencia de entrega (POD - Proof of Delivery)';
COMMENT ON COLUMN paquetes.nombre_firmante IS 'Nombre de la persona que recibe el paquete';
COMMENT ON COLUMN paquetes.fecha_entrega_utc IS 'Timestamp UTC del momento de entrega del paquete';

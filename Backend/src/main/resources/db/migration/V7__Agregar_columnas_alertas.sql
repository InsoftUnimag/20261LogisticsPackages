-- V7: Agregar columnas de alertas y etiqueta digital a la tabla paquetes
-- Fix: Persistir alertas de carga especial, densidad atípica y etiqueta digital

ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS alerta_carga_especial BOOLEAN DEFAULT FALSE;
ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS alerta_densidad_atipica BOOLEAN DEFAULT FALSE;
ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS etiqueta_digital VARCHAR(50);

COMMENT ON COLUMN paquetes.alerta_carga_especial IS 'Indica si el paquete requiere manejo especial por peso o volumen';
COMMENT ON COLUMN paquetes.alerta_densidad_atipica IS 'Indica si la densidad del paquete es atípica (>30% diferencia)';
COMMENT ON COLUMN paquetes.etiqueta_digital IS 'Etiqueta digital única vinculada al UUID del paquete';
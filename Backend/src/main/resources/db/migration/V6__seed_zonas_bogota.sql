--Seed Data: Zonas de Almacenaje y Destino OPTIMIZADAS para Bogotá
--Fecha: 2026-05-22
--Descripción: 4 zonas de almacenaje (Normal, Delicada, Alto Riesgo, Retención) + 21 zonas de destino por localidades
--Actualización: Simplificadas zonas de almacenaje para alinearse con otras sedes (4 por sede)

-- ============================================
-- ZONAS DE ALMACENAJE - BOGOTÁ (4 zonas solamente)
-- ============================================

INSERT INTO zonas_almacenaje (id, nombre, codigo, categoria, capacidad_max_kg, capacidad_max_m3, capacidad_max_paquetes, peso_actual_kg, volumen_actual_m3, contador_paquetes, estado, ubicacion_fisica, id_sede, zona_contingencia_id)
VALUES 
-- Zona NORMAL - Bogotá
('770e8400-e29b-41d4-a716-446655440001', 'Zona Normal Bogotá', 'ZA-BOG-NORMAL', 'NORMAL', 5000.00, 100.000, 400, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo A-B', '550e8400-e29b-41d4-a716-446655440001', NULL),

-- Zona DELICADA - Bogotá
('770e8400-e29b-41d4-a716-446655440002', 'Zona Delicada Bogotá', 'ZA-BOG-DELICADA', 'DELICADA', 1500.00, 30.000, 100, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo C climatizado', '550e8400-e29b-41d4-a716-446655440001', NULL),

-- Zona ALTO_RIESGO - Bogotá
('770e8400-e29b-41d4-a716-446655440003', 'Zona Alto Riesgo Bogotá', 'ZA-BOG-RIESGO', 'ALTO_RIESGO', 2000.00, 50.000, 80, 0.00, 0.000, 0, 'DISPONIBLE', 'Área restringida D', '550e8400-e29b-41d4-a716-446655440001', NULL),

-- Zona RETENCION - Bogotá
('770e8400-e29b-41d4-a716-446655440004', 'Zona Retención Bogotá', 'ZA-BOG-RETENCION', 'RETENCION', 800.00, 20.000, 50, 0.00, 0.000, 0, 'DISPONIBLE', 'Cuarto de retención E', '550e8400-e29b-41d4-a716-446655440001', NULL)
ON CONFLICT DO NOTHING;

-- ============================================
-- ZONAS DE DESTINO - BOGOTÁ EXPANDIDAS (21 localidades + 1 fallback)
-- Todas las zonas de destino son NORMAL para permitir cualquier tipo de mercancía
-- La restricción de tipo de mercancía se maneja en las zonas de almacenaje
-- ============================================

INSERT INTO zonas_destino (id, nombre, codigo, categoria, latitud_min, latitud_max, longitud_min, longitud_max, capacidad_max_paquetes, contador_paquetes, id_sede)
VALUES 
-- NORTE DE BOGOTÁ
('660e8400-e29b-41d4-a716-446655440030', 'Usaquén', 'ZD-BOG-USQ', 'NORMAL', 4.7000, 4.8000, -74.0500, -73.9800, 600, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440031', 'Suba', 'ZD-BOG-SUB', 'NORMAL', 4.7500, 4.8500, -74.1500, -74.0400, 700, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440032', 'Barrios Unidos', 'ZD-BOG-BUN', 'NORMAL', 4.6800, 4.7200, -74.0950, -74.0400, 350, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440033', 'Chapinero', 'ZD-BOG-CHP', 'NORMAL', 4.6200, 4.6700, -74.0700, -74.0200, 400, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440034', 'Teusaquillo', 'ZD-BOG-TUS', 'NORMAL', 4.6700, 4.7100, -74.0950, -74.0500, 300, 0, '550e8400-e29b-41d4-a716-446655440001'),

-- CENTRO DE BOGOTÁ
('660e8400-e29b-41d4-a716-446655440035', 'Santa Fe', 'ZD-BOG-STF', 'NORMAL', 4.5900, 4.6300, -74.0800, -74.0400, 350, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440036', 'La Candelaria', 'ZD-BOG-CAN', 'NORMAL', 4.5850, 4.6000, -74.0800, -74.0600, 150, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440037', 'Los Mártires', 'ZD-BOG-MAR', 'NORMAL', 4.5950, 4.6250, -74.1000, -74.0700, 250, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440038', 'Antonio Nariño', 'ZD-BOG-ANT', 'NORMAL', 4.5600, 4.5850, -74.1050, -74.0700, 300, 0, '550e8400-e29b-41d4-a716-446655440001'),

-- OCCIDENTE DE BOGOTÁ
('660e8400-e29b-41d4-a716-446655440039', 'Fontibón', 'ZD-BOG-FON', 'NORMAL', 4.6400, 4.7100, -74.1600, -74.0900, 450, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440040', 'Engativá', 'ZD-BOG-ENG', 'NORMAL', 4.6900, 4.7600, -74.1400, -74.0700, 550, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440041', 'Puente Aranda', 'ZD-BOG-PTA', 'NORMAL', 4.6200, 4.6700, -74.1400, -74.0900, 350, 0, '550e8400-e29b-41d4-a716-446655440001'),

-- SUR Y SUROCCIDENTE DE BOGOTÁ
('660e8400-e29b-41d4-a716-446655440042', 'Kennedy', 'ZD-BOG-KEN', 'NORMAL', 4.5900, 4.6600, -74.1700, -74.1000, 750, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440043', 'Bosa', 'ZD-BOG-BSA', 'NORMAL', 4.5500, 4.6000, -74.2000, -74.1300, 550, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440044', 'Ciudad Bolívar', 'ZD-BOG-CBO', 'NORMAL', 4.5000, 4.5450, -74.2000, -74.1000, 600, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440045', 'Tunjuelito', 'ZD-BOG-TUN', 'NORMAL', 4.5450, 4.5800, -74.1300, -74.1000, 350, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440046', 'Usme', 'ZD-BOG-USM', 'NORMAL', 4.5100, 4.5500, -74.1500, -74.0500, 450, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440047', 'Sumapaz', 'ZD-BOG-SMP', 'NORMAL', 3.9500, 4.4200, -74.4000, -74.0000, 200, 0, '550e8400-e29b-41d4-a716-446655440001'),

-- ORIENTE / CERROS DE BOGOTÁ
('660e8400-e29b-41d4-a716-446655440048', 'San Cristóbal', 'ZD-BOG-SCR', 'NORMAL', 4.5850, 4.6200, -74.0900, -74.0600, 350, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440049', 'Rafael Uribe Uribe', 'ZD-BOG-RUU', 'NORMAL', 4.5600, 4.5850, -74.1100, -74.0800, 350, 0, '550e8400-e29b-41d4-a716-446655440001'),

-- FALLBACK: Bogotá General (cubre toda la ciudad para evitar ZonaNoEncontradaException)
('660e8400-e29b-41d4-a716-446655440050', 'Bogotá General', 'ZD-BOG-GEN', 'NORMAL', 4.3000, 4.9000, -74.3000, -73.9000, 5000, 0, '550e8400-e29b-41d4-a716-446655440001')
ON CONFLICT DO NOTHING;

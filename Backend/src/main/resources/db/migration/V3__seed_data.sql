--Seed Data para tablas maestras necesarias
--Fecha: 2026-05-01
--Actualización: Ahora con 11 sedes en ciudades principales de Colombia, coordenadas, y 4 zonas de almacenaje por sede

-- ============================================
-- SEDES (11 ciudades principales de Colombia)
-- ============================================

INSERT INTO sedes (id, nombre, direccion, ciudad, departamento, pais, tipo, capacidad_maxima_peso, capacidad_maxima_volumen, tarifa_base, tarifa_por_kg, tarifa_por_km, metodos_pago_habilitados, latitud, longitud, fecha_creacion)
VALUES 
-- Sede 001: Bogotá (PRINCIPAL)
(
    '550e8400-e29b-41d4-a716-446655440001',
    'Sede Principal Bogotá',
    'Carrera 10 #20-30',
    'Bogotá',
    'Cundinamarca',
    'Colombia',
    'PRINCIPAL',
    10000.00,
    100.000,
    5000.00,
    1500.00,
    100.00,
    ARRAY['PREPAGO', 'CONTRA_ENTREGA'],
    4.624335,
    -74.063644,
    CURRENT_TIMESTAMP
),
-- Sede 002: Medellín (AUXILIAR)
(
    '550e8400-e29b-41d4-a716-446655440002',
    'Sede Auxiliar Medellín',
    'Calle 45 #8-30',
    'Medellín',
    'Antioquia',
    'Colombia',
    'AUXILIAR',
    5000.00,
    50.000,
    5000.00,
    1500.00,
    100.00,
    ARRAY['PREPAGO'],
    6.244747,
    -75.573553,
    CURRENT_TIMESTAMP
),
-- Sede 003: Cali (PRINCIPAL)
(
    '550e8400-e29b-41d4-a716-446655440003',
    'Sede Principal Cali',
    'Avenida Roosevelt #25-50',
    'Cali',
    'Valle del Cauca',
    'Colombia',
    'PRINCIPAL',
    7500.00,
    75.000,
    5000.00,
    1500.00,
    100.00,
    ARRAY['PREPAGO', 'CONTRA_ENTREGA'],
    3.451647,
    -76.531985,
    CURRENT_TIMESTAMP
),
-- Sede 004: Barranquilla (PRINCIPAL)
(
    '550e8400-e29b-41d4-a716-446655440004',
    'Sede Principal Barranquilla',
    'Carrera 53 #79-12',
    'Barranquilla',
    'Atlántico',
    'Colombia',
    'PRINCIPAL',
    6000.00,
    60.000,
    5000.00,
    1500.00,
    100.00,
    ARRAY['PREPAGO', 'CONTRA_ENTREGA'],
    10.964671,
    -74.796387,
    CURRENT_TIMESTAMP
),
-- Sede 005: Cartagena (AUXILIAR)
(
    '550e8400-e29b-41d4-a716-446655440005',
    'Sede Auxiliar Cartagena',
    'Calle 31 #8-15',
    'Cartagena',
    'Bolívar',
    'Colombia',
    'AUXILIAR',
    4000.00,
    40.000,
    5000.00,
    1500.00,
    100.00,
    ARRAY['PREPAGO'],
    10.391049,
    -75.479426,
    CURRENT_TIMESTAMP
),
-- Sede 006: Bucaramanga (AUXILIAR)
(
    '550e8400-e29b-41d4-a716-446655440006',
    'Sede Auxiliar Bucaramanga',
    'Calle 35 #10-20',
    'Bucaramanga',
    'Santander',
    'Colombia',
    'AUXILIAR',
    4500.00,
    45.000,
    5000.00,
    1500.00,
    100.00,
    ARRAY['PREPAGO'],
    7.119349,
    -73.122742,
    CURRENT_TIMESTAMP
),
-- Sede 007: Pereira (AUXILIAR)
(
    '550e8400-e29b-41d4-a716-446655440007',
    'Sede Auxiliar Pereira',
    'Calle 20 #9-27',
    'Pereira',
    'Risaralda',
    'Colombia',
    'AUXILIAR',
    3500.00,
    35.000,
    5000.00,
    1500.00,
    100.00,
    ARRAY['PREPAGO'],
    4.813517,
    -75.696121,
    CURRENT_TIMESTAMP
),
-- Sede 008: Manizales (AUXILIAR)
(
    '550e8400-e29b-41d4-a716-446655440008',
    'Sede Auxiliar Manizales',
    'Avenida 19 #20-30',
    'Manizales',
    'Caldas',
    'Colombia',
    'AUXILIAR',
    3500.00,
    35.000,
    5000.00,
    1500.00,
    100.00,
    ARRAY['PREPAGO'],
    5.070162,
    -75.513399,
    CURRENT_TIMESTAMP
),
-- Sede 009: Cúcuta (AUXILIAR)
(
    '550e8400-e29b-41d4-a716-446655440009',
    'Sede Auxiliar Cúcuta',
    'Avenida 4 #13-20',
    'Cúcuta',
    'Norte de Santander',
    'Colombia',
    'AUXILIAR',
    4000.00,
    40.000,
    5000.00,
    1500.00,
    100.00,
    ARRAY['PREPAGO'],
    7.893107,
    -72.504637,
    CURRENT_TIMESTAMP
),
-- Sede 010: Ibagué (AUXILIAR)
(
    '550e8400-e29b-41d4-a716-446655440010',
    'Sede Auxiliar Ibagué',
    'Carrera 4 #13-40',
    'Ibagué',
    'Tolima',
    'Colombia',
    'AUXILIAR',
    3500.00,
    35.000,
    5000.00,
    1500.00,
    100.00,
    ARRAY['PREPAGO'],
    4.438889,
    -75.232222,
    CURRENT_TIMESTAMP
),
-- Sede 011: Riohacha (AUXILIAR)
(
    '550e8400-e29b-41d4-a716-446655440011',
    'Sede Auxiliar Riohacha',
    'Calle 1 #2-50',
    'Riohacha',
    'La Guajira',
    'Colombia',
    'AUXILIAR',
    3000.00,
    30.000,
    5000.00,
    1500.00,
    100.00,
    ARRAY['PREPAGO'],
    11.544396,
    -72.907148,
    CURRENT_TIMESTAMP
);

-- ============================================
-- ZONAS DE DESTINO
-- Se definen zonas de destino por ciudad para cubrir geografía
-- Formato: 2-4 zonas por ciudad (Norte, Sur, Centro, Oriente según aplique)
-- ============================================

INSERT INTO zonas_destino (id, nombre, codigo, categoria, latitud_min, latitud_max, longitud_min, longitud_max, capacidad_max_paquetes, contador_paquetes, id_sede)
VALUES 
-- BOGOTÁ (5 zonas principales + 1 fallback)
('660e8400-e29b-41d4-a716-446655440001', 'Bogotá Centro', 'ZD-BOG-CENTRO', 'NORMAL', 4.57, 4.70, -74.15, -73.98, 500, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440002', 'Bogotá Norte', 'ZD-BOG-NORTE', 'NORMAL', 4.70, 4.85, -74.15, -73.98, 400, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440003', 'Bogotá Sur', 'ZD-BOG-SUR', 'NORMAL', 4.40, 4.57, -74.15, -73.98, 400, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440004', 'Bogotá Occidente', 'ZD-BOG-OESTE', 'NORMAL', 4.57, 4.75, -74.25, -74.15, 350, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440005', 'Bogotá Oriente', 'ZD-BOG-ORIENTE', 'NORMAL', 4.57, 4.75, -73.98, -73.85, 300, 0, '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440006', 'Bogotá General', 'ZD-BOG-FALLBACK', 'NORMAL', 4.30, 4.90, -74.30, -73.80, 2000, 0, '550e8400-e29b-41d4-a716-446655440001'),

-- MEDELLÍN (3 zonas + 1 fallback)
('660e8400-e29b-41d4-a716-446655440007', 'Medellín Norte', 'ZD-MED-NORTE', 'NORMAL', 6.30, 6.40, -75.60, -75.50, 300, 0, '550e8400-e29b-41d4-a716-446655440002'),
('660e8400-e29b-41d4-a716-446655440008', 'Medellín Centro', 'ZD-MED-CENTRO', 'NORMAL', 6.15, 6.30, -75.60, -75.50, 300, 0, '550e8400-e29b-41d4-a716-446655440002'),
('660e8400-e29b-41d4-a716-446655440009', 'Medellín Sur', 'ZD-MED-SUR', 'NORMAL', 6.00, 6.15, -75.60, -75.50, 250, 0, '550e8400-e29b-41d4-a716-446655440002'),
('660e8400-e29b-41d4-a716-446655440010', 'Medellín General', 'ZD-MED-FALLBACK', 'NORMAL', 5.90, 6.50, -75.70, -75.40, 1500, 0, '550e8400-e29b-41d4-a716-446655440002'),

-- CALI (3 zonas + 1 fallback)
('660e8400-e29b-41d4-a716-446655440011', 'Cali Norte', 'ZD-CALI-NORTE', 'NORMAL', 3.55, 3.65, -76.55, -76.45, 300, 0, '550e8400-e29b-41d4-a716-446655440003'),
('660e8400-e29b-41d4-a716-446655440012', 'Cali Centro', 'ZD-CALI-CENTRO', 'NORMAL', 3.40, 3.55, -76.55, -76.45, 300, 0, '550e8400-e29b-41d4-a716-446655440003'),
('660e8400-e29b-41d4-a716-446655440013', 'Cali Sur', 'ZD-CALI-SUR', 'NORMAL', 3.25, 3.40, -76.55, -76.45, 250, 0, '550e8400-e29b-41d4-a716-446655440003'),
('660e8400-e29b-41d4-a716-446655440014', 'Cali General', 'ZD-CALI-FALLBACK', 'NORMAL', 3.15, 3.75, -76.65, -76.35, 1500, 0, '550e8400-e29b-41d4-a716-446655440003'),

-- BARRANQUILLA (3 zonas + 1 fallback)
('660e8400-e29b-41d4-a716-446655440015', 'Barranquilla Norte', 'ZD-BAR-NORTE', 'NORMAL', 11.05, 11.15, -74.90, -74.70, 300, 0, '550e8400-e29b-41d4-a716-446655440004'),
('660e8400-e29b-41d4-a716-446655440016', 'Barranquilla Centro', 'ZD-BAR-CENTRO', 'NORMAL', 10.95, 11.05, -74.90, -74.70, 300, 0, '550e8400-e29b-41d4-a716-446655440004'),
('660e8400-e29b-41d4-a716-446655440017', 'Barranquilla Sur', 'ZD-BAR-SUR', 'NORMAL', 10.85, 10.95, -74.90, -74.70, 250, 0, '550e8400-e29b-41d4-a716-446655440004'),
('660e8400-e29b-41d4-a716-446655440018', 'Barranquilla General', 'ZD-BAR-FALLBACK', 'NORMAL', 10.75, 11.25, -75.00, -74.60, 1500, 0, '550e8400-e29b-41d4-a716-446655440004'),

-- CARTAGENA (2 zonas + 1 fallback)
('660e8400-e29b-41d4-a716-446655440019', 'Cartagena Centro', 'ZD-CTG-CENTRO', 'NORMAL', 10.35, 10.45, -75.55, -75.40, 250, 0, '550e8400-e29b-41d4-a716-446655440005'),
('660e8400-e29b-41d4-a716-446655440020', 'Cartagena Periferia', 'ZD-CTG-PERIFERIA', 'NORMAL', 10.25, 10.35, -75.55, -75.40, 200, 0, '550e8400-e29b-41d4-a716-446655440005'),
('660e8400-e29b-41d4-a716-446655440021', 'Cartagena General', 'ZD-CTG-FALLBACK', 'NORMAL', 10.15, 10.55, -75.65, -75.30, 1000, 0, '550e8400-e29b-41d4-a716-446655440005'),

-- BUCARAMANGA (2 zonas + 1 fallback)
('660e8400-e29b-41d4-a716-446655440022', 'Bucaramanga Centro', 'ZD-BUC-CENTRO', 'NORMAL', 7.10, 7.15, -73.15, -73.10, 250, 0, '550e8400-e29b-41d4-a716-446655440006'),
('660e8400-e29b-41d4-a716-446655440023', 'Bucaramanga Periferia', 'ZD-BUC-PERIFERIA', 'NORMAL', 7.05, 7.10, -73.15, -73.10, 200, 0, '550e8400-e29b-41d4-a716-446655440006'),
('660e8400-e29b-41d4-a716-446655440024', 'Bucaramanga General', 'ZD-BUC-FALLBACK', 'NORMAL', 6.95, 7.25, -73.25, -73.00, 1000, 0, '550e8400-e29b-41d4-a716-446655440006'),

-- PEREIRA (2 zonas + 1 fallback)
('660e8400-e29b-41d4-a716-446655440025', 'Pereira Centro', 'ZD-PER-CENTRO', 'NORMAL', 4.80, 4.85, -75.75, -75.65, 200, 0, '550e8400-e29b-41d4-a716-446655440007'),
('660e8400-e29b-41d4-a716-446655440026', 'Pereira Periferia', 'ZD-PER-PERIFERIA', 'NORMAL', 4.75, 4.80, -75.75, -75.65, 150, 0, '550e8400-e29b-41d4-a716-446655440007'),
('660e8400-e29b-41d4-a716-446655440027', 'Pereira General', 'ZD-PER-FALLBACK', 'NORMAL', 4.70, 4.95, -75.85, -75.55, 800, 0, '550e8400-e29b-41d4-a716-446655440007'),

-- MANIZALES (2 zonas + 1 fallback)
('660e8400-e29b-41d4-a716-446655440028', 'Manizales Centro', 'ZD-MAN-CENTRO', 'NORMAL', 5.05, 5.10, -75.55, -75.45, 200, 0, '550e8400-e29b-41d4-a716-446655440008'),
('660e8400-e29b-41d4-a716-446655440029', 'Manizales Periferia', 'ZD-MAN-PERIFERIA', 'NORMAL', 5.00, 5.05, -75.55, -75.45, 150, 0, '550e8400-e29b-41d4-a716-446655440008'),
('660e8400-e29b-41d4-a716-446655440030', 'Manizales General', 'ZD-MAN-FALLBACK', 'NORMAL', 4.95, 5.20, -75.65, -75.35, 800, 0, '550e8400-e29b-41d4-a716-446655440008'),

-- CÚCUTA (2 zonas + 1 fallback)
('660e8400-e29b-41d4-a716-446655440031', 'Cúcuta Centro', 'ZD-CUC-CENTRO', 'NORMAL', 7.87, 7.92, -72.55, -72.45, 250, 0, '550e8400-e29b-41d4-a716-446655440009'),
('660e8400-e29b-41d4-a716-446655440032', 'Cúcuta Periferia', 'ZD-CUC-PERIFERIA', 'NORMAL', 7.82, 7.87, -72.55, -72.45, 200, 0, '550e8400-e29b-41d4-a716-446655440009'),
('660e8400-e29b-41d4-a716-446655440033', 'Cúcuta General', 'ZD-CUC-FALLBACK', 'NORMAL', 7.75, 8.05, -72.65, -72.35, 1000, 0, '550e8400-e29b-41d4-a716-446655440009'),

-- IBAGUÉ (2 zonas + 1 fallback)
('660e8400-e29b-41d4-a716-446655440034', 'Ibagué Centro', 'ZD-IBA-CENTRO', 'NORMAL', 4.42, 4.47, -75.25, -75.20, 200, 0, '550e8400-e29b-41d4-a716-446655440010'),
('660e8400-e29b-41d4-a716-446655440035', 'Ibagué Periferia', 'ZD-IBA-PERIFERIA', 'NORMAL', 4.37, 4.42, -75.25, -75.20, 150, 0, '550e8400-e29b-41d4-a716-446655440010'),
('660e8400-e29b-41d4-a716-446655440036', 'Ibagué General', 'ZD-IBA-FALLBACK', 'NORMAL', 4.30, 4.55, -75.35, -75.10, 800, 0, '550e8400-e29b-41d4-a716-446655440010'),

-- RIOHACHA (2 zonas + 1 fallback)
('660e8400-e29b-41d4-a716-446655440037', 'Riohacha Centro', 'ZD-RIO-CENTRO', 'NORMAL', 11.52, 11.57, -72.92, -72.87, 150, 0, '550e8400-e29b-41d4-a716-446655440011'),
('660e8400-e29b-41d4-a716-446655440038', 'Riohacha Periferia', 'ZD-RIO-PERIFERIA', 'NORMAL', 11.47, 11.52, -72.92, -72.87, 100, 0, '550e8400-e29b-41d4-a716-446655440011'),
('660e8400-e29b-41d4-a716-446655440039', 'Riohacha General', 'ZD-RIO-FALLBACK', 'NORMAL', 11.40, 11.65, -73.00, -72.80, 600, 0, '550e8400-e29b-41d4-a716-446655440011');

-- ============================================
-- ZONAS DE ALMACENAJE (4 por cada sede)
-- Cada sede tiene: NORMAL, DELICADA, ALTO_RIESGO, RETENCION
-- ============================================

INSERT INTO zonas_almacenaje (id, nombre, codigo, categoria, capacidad_max_kg, capacidad_max_m3, capacidad_max_paquetes, peso_actual_kg, volumen_actual_m3, contador_paquetes, estado, ubicacion_fisica, id_sede, zona_contingencia_id)
VALUES 
-- BOGOTÁ - 4 zonas
('770e8400-e29b-41d4-a716-446655440001', 'Zona Normal Bogotá', 'ZA-BOG-NORMAL', 'NORMAL', 5000.00, 100.000, 400, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo A-B', '550e8400-e29b-41d4-a716-446655440001', NULL),
('770e8400-e29b-41d4-a716-446655440002', 'Zona Delicada Bogotá', 'ZA-BOG-DELICADA', 'DELICADA', 1500.00, 30.000, 100, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo C climatizado', '550e8400-e29b-41d4-a716-446655440001', NULL),
('770e8400-e29b-41d4-a716-446655440003', 'Zona Alto Riesgo Bogotá', 'ZA-BOG-RIESGO', 'ALTO_RIESGO', 2000.00, 50.000, 80, 0.00, 0.000, 0, 'DISPONIBLE', 'Área restringida D', '550e8400-e29b-41d4-a716-446655440001', NULL),
('770e8400-e29b-41d4-a716-446655440004', 'Zona Retención Bogotá', 'ZA-BOG-RETENCION', 'RETENCION', 800.00, 20.000, 50, 0.00, 0.000, 0, 'DISPONIBLE', 'Cuarto de retención E', '550e8400-e29b-41d4-a716-446655440001', NULL),

-- MEDELLÍN - 4 zonas
('770e8400-e29b-41d4-a716-446655440005', 'Zona Normal Medellín', 'ZA-MED-NORMAL', 'NORMAL', 3000.00, 60.000, 250, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo 1-2', '550e8400-e29b-41d4-a716-446655440002', NULL),
('770e8400-e29b-41d4-a716-446655440006', 'Zona Delicada Medellín', 'ZA-MED-DELICADA', 'DELICADA', 800.00, 16.000, 60, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo 3 climatizado', '550e8400-e29b-41d4-a716-446655440002', NULL),
('770e8400-e29b-41d4-a716-446655440007', 'Zona Alto Riesgo Medellín', 'ZA-MED-RIESGO', 'ALTO_RIESGO', 1200.00, 30.000, 50, 0.00, 0.000, 0, 'DISPONIBLE', 'Área restringida 4', '550e8400-e29b-41d4-a716-446655440002', NULL),
('770e8400-e29b-41d4-a716-446655440008', 'Zona Retención Medellín', 'ZA-MED-RETENCION', 'RETENCION', 500.00, 12.000, 30, 0.00, 0.000, 0, 'DISPONIBLE', 'Cuarto 5', '550e8400-e29b-41d4-a716-446655440002', NULL),

-- CALI - 4 zonas
('770e8400-e29b-41d4-a716-446655440009', 'Zona Normal Cali', 'ZA-CALI-NORMAL', 'NORMAL', 3500.00, 70.000, 280, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo A-B', '550e8400-e29b-41d4-a716-446655440003', NULL),
('770e8400-e29b-41d4-a716-446655440010', 'Zona Delicada Cali', 'ZA-CALI-DELICADA', 'DELICADA', 1000.00, 20.000, 75, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo C climatizado', '550e8400-e29b-41d4-a716-446655440003', NULL),
('770e8400-e29b-41d4-a716-446655440011', 'Zona Alto Riesgo Cali', 'ZA-CALI-RIESGO', 'ALTO_RIESGO', 1500.00, 35.000, 60, 0.00, 0.000, 0, 'DISPONIBLE', 'Área restringida D', '550e8400-e29b-41d4-a716-446655440003', NULL),
('770e8400-e29b-41d4-a716-446655440012', 'Zona Retención Cali', 'ZA-CALI-RETENCION', 'RETENCION', 600.00, 15.000, 40, 0.00, 0.000, 0, 'DISPONIBLE', 'Cuarto E', '550e8400-e29b-41d4-a716-446655440003', NULL),

-- BARRANQUILLA - 4 zonas
('770e8400-e29b-41d4-a716-446655440013', 'Zona Normal Barranquilla', 'ZA-BAR-NORMAL', 'NORMAL', 2500.00, 50.000, 200, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo A', '550e8400-e29b-41d4-a716-446655440004', NULL),
('770e8400-e29b-41d4-a716-446655440014', 'Zona Delicada Barranquilla', 'ZA-BAR-DELICADA', 'DELICADA', 700.00, 14.000, 50, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo B climatizado', '550e8400-e29b-41d4-a716-446655440004', NULL),
('770e8400-e29b-41d4-a716-446655440015', 'Zona Alto Riesgo Barranquilla', 'ZA-BAR-RIESGO', 'ALTO_RIESGO', 1000.00, 25.000, 40, 0.00, 0.000, 0, 'DISPONIBLE', 'Área restringida C', '550e8400-e29b-41d4-a716-446655440004', NULL),
('770e8400-e29b-41d4-a716-446655440016', 'Zona Retención Barranquilla', 'ZA-BAR-RETENCION', 'RETENCION', 400.00, 10.000, 25, 0.00, 0.000, 0, 'DISPONIBLE', 'Cuarto D', '550e8400-e29b-41d4-a716-446655440004', NULL),

-- CARTAGENA - 4 zonas
('770e8400-e29b-41d4-a716-446655440017', 'Zona Normal Cartagena', 'ZA-CTG-NORMAL', 'NORMAL', 2000.00, 40.000, 160, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo 1', '550e8400-e29b-41d4-a716-446655440005', NULL),
('770e8400-e29b-41d4-a716-446655440018', 'Zona Delicada Cartagena', 'ZA-CTG-DELICADA', 'DELICADA', 500.00, 10.000, 40, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo 2 climatizado', '550e8400-e29b-41d4-a716-446655440005', NULL),
('770e8400-e29b-41d4-a716-446655440019', 'Zona Alto Riesgo Cartagena', 'ZA-CTG-RIESGO', 'ALTO_RIESGO', 800.00, 20.000, 32, 0.00, 0.000, 0, 'DISPONIBLE', 'Área restringida 3', '550e8400-e29b-41d4-a716-446655440005', NULL),
('770e8400-e29b-41d4-a716-446655440020', 'Zona Retención Cartagena', 'ZA-CTG-RETENCION', 'RETENCION', 300.00, 8.000, 20, 0.00, 0.000, 0, 'DISPONIBLE', 'Cuarto 4', '550e8400-e29b-41d4-a716-446655440005', NULL),

-- BUCARAMANGA - 4 zonas
('770e8400-e29b-41d4-a716-446655440021', 'Zona Normal Bucaramanga', 'ZA-BUC-NORMAL', 'NORMAL', 2200.00, 44.000, 176, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo A', '550e8400-e29b-41d4-a716-446655440006', NULL),
('770e8400-e29b-41d4-a716-446655440022', 'Zona Delicada Bucaramanga', 'ZA-BUC-DELICADA', 'DELICADA', 550.00, 11.000, 44, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo B climatizado', '550e8400-e29b-41d4-a716-446655440006', NULL),
('770e8400-e29b-41d4-a716-446655440023', 'Zona Alto Riesgo Bucaramanga', 'ZA-BUC-RIESGO', 'ALTO_RIESGO', 900.00, 22.000, 36, 0.00, 0.000, 0, 'DISPONIBLE', 'Área restringida C', '550e8400-e29b-41d4-a716-446655440006', NULL),
('770e8400-e29b-41d4-a716-446655440024', 'Zona Retención Bucaramanga', 'ZA-BUC-RETENCION', 'RETENCION', 350.00, 9.000, 22, 0.00, 0.000, 0, 'DISPONIBLE', 'Cuarto D', '550e8400-e29b-41d4-a716-446655440006', NULL),

-- PEREIRA - 4 zonas
('770e8400-e29b-41d4-a716-446655440025', 'Zona Normal Pereira', 'ZA-PER-NORMAL', 'NORMAL', 1750.00, 35.000, 140, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo 1', '550e8400-e29b-41d4-a716-446655440007', NULL),
('770e8400-e29b-41d4-a716-446655440026', 'Zona Delicada Pereira', 'ZA-PER-DELICADA', 'DELICADA', 450.00, 9.000, 36, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo 2 climatizado', '550e8400-e29b-41d4-a716-446655440007', NULL),
('770e8400-e29b-41d4-a716-446655440027', 'Zona Alto Riesgo Pereira', 'ZA-PER-RIESGO', 'ALTO_RIESGO', 700.00, 18.000, 28, 0.00, 0.000, 0, 'DISPONIBLE', 'Área restringida 3', '550e8400-e29b-41d4-a716-446655440007', NULL),
('770e8400-e29b-41d4-a716-446655440028', 'Zona Retención Pereira', 'ZA-PER-RETENCION', 'RETENCION', 280.00, 7.000, 18, 0.00, 0.000, 0, 'DISPONIBLE', 'Cuarto 4', '550e8400-e29b-41d4-a716-446655440007', NULL),

-- MANIZALES - 4 zonas
('770e8400-e29b-41d4-a716-446655440029', 'Zona Normal Manizales', 'ZA-MAN-NORMAL', 'NORMAL', 1750.00, 35.000, 140, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo A', '550e8400-e29b-41d4-a716-446655440008', NULL),
('770e8400-e29b-41d4-a716-446655440030', 'Zona Delicada Manizales', 'ZA-MAN-DELICADA', 'DELICADA', 450.00, 9.000, 36, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo B climatizado', '550e8400-e29b-41d4-a716-446655440008', NULL),
('770e8400-e29b-41d4-a716-446655440031', 'Zona Alto Riesgo Manizales', 'ZA-MAN-RIESGO', 'ALTO_RIESGO', 700.00, 18.000, 28, 0.00, 0.000, 0, 'DISPONIBLE', 'Área restringida C', '550e8400-e29b-41d4-a716-446655440008', NULL),
('770e8400-e29b-41d4-a716-446655440032', 'Zona Retención Manizales', 'ZA-MAN-RETENCION', 'RETENCION', 280.00, 7.000, 18, 0.00, 0.000, 0, 'DISPONIBLE', 'Cuarto D', '550e8400-e29b-41d4-a716-446655440008', NULL),

-- CÚCUTA - 4 zonas
('770e8400-e29b-41d4-a716-446655440033', 'Zona Normal Cúcuta', 'ZA-CUC-NORMAL', 'NORMAL', 2000.00, 40.000, 160, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo 1', '550e8400-e29b-41d4-a716-446655440009', NULL),
('770e8400-e29b-41d4-a716-446655440034', 'Zona Delicada Cúcuta', 'ZA-CUC-DELICADA', 'DELICADA', 500.00, 10.000, 40, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo 2 climatizado', '550e8400-e29b-41d4-a716-446655440009', NULL),
('770e8400-e29b-41d4-a716-446655440035', 'Zona Alto Riesgo Cúcuta', 'ZA-CUC-RIESGO', 'ALTO_RIESGO', 800.00, 20.000, 32, 0.00, 0.000, 0, 'DISPONIBLE', 'Área restringida 3', '550e8400-e29b-41d4-a716-446655440009', NULL),
('770e8400-e29b-41d4-a716-446655440036', 'Zona Retención Cúcuta', 'ZA-CUC-RETENCION', 'RETENCION', 300.00, 8.000, 20, 0.00, 0.000, 0, 'DISPONIBLE', 'Cuarto 4', '550e8400-e29b-41d4-a716-446655440009', NULL),

-- IBAGUÉ - 4 zonas
('770e8400-e29b-41d4-a716-446655440037', 'Zona Normal Ibagué', 'ZA-IBA-NORMAL', 'NORMAL', 1750.00, 35.000, 140, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo A', '550e8400-e29b-41d4-a716-446655440010', NULL),
('770e8400-e29b-41d4-a716-446655440038', 'Zona Delicada Ibagué', 'ZA-IBA-DELICADA', 'DELICADA', 450.00, 9.000, 36, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo B climatizado', '550e8400-e29b-41d4-a716-446655440010', NULL),
('770e8400-e29b-41d4-a716-446655440039', 'Zona Alto Riesgo Ibagué', 'ZA-IBA-RIESGO', 'ALTO_RIESGO', 700.00, 18.000, 28, 0.00, 0.000, 0, 'DISPONIBLE', 'Área restringida C', '550e8400-e29b-41d4-a716-446655440010', NULL),
('770e8400-e29b-41d4-a716-446655440040', 'Zona Retención Ibagué', 'ZA-IBA-RETENCION', 'RETENCION', 280.00, 7.000, 18, 0.00, 0.000, 0, 'DISPONIBLE', 'Cuarto D', '550e8400-e29b-41d4-a716-446655440010', NULL),

-- RIOHACHA - 4 zonas
('770e8400-e29b-41d4-a716-446655440041', 'Zona Normal Riohacha', 'ZA-RIO-NORMAL', 'NORMAL', 1500.00, 30.000, 120, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo 1', '550e8400-e29b-41d4-a716-446655440011', NULL),
('770e8400-e29b-41d4-a716-446655440042', 'Zona Delicada Riohacha', 'ZA-RIO-DELICADA', 'DELICADA', 400.00, 8.000, 32, 0.00, 0.000, 0, 'DISPONIBLE', 'Pasillo 2 climatizado', '550e8400-e29b-41d4-a716-446655440011', NULL),
('770e8400-e29b-41d4-a716-446655440043', 'Zona Alto Riesgo Riohacha', 'ZA-RIO-RIESGO', 'ALTO_RIESGO', 600.00, 15.000, 24, 0.00, 0.000, 0, 'DISPONIBLE', 'Área restringida 3', '550e8400-e29b-41d4-a716-446655440011', NULL),
('770e8400-e29b-41d4-a716-446655440044', 'Zona Retención Riohacha', 'ZA-RIO-RETENCION', 'RETENCION', 250.00, 6.000, 16, 0.00, 0.000, 0, 'DISPONIBLE', 'Cuarto 4', '550e8400-e29b-41d4-a716-446655440011', NULL);

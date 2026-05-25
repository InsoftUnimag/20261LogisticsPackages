--Seed Data para tablas maestras necesarias
--Fecha: 2026-05-01

-- ============================================
-- SEDES
-- ============================================

INSERT INTO sedes (id, nombre, direccion, ciudad, departamento, pais, tipo, capacidad_maxima_peso, capacidad_maxima_volumen, tarifa_base, tarifa_por_kg, tarifa_por_km, metodos_pago_habilitados, fecha_creacion)
VALUES 
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
    CURRENT_TIMESTAMP
),
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
    CURRENT_TIMESTAMP
),
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
    CURRENT_TIMESTAMP
);

-- ============================================
-- ZONAS DE DESTINO - MEDELLÍN Y CALI
-- (Las zonas de destino de BOGOTÁ están en V6__seed_zonas_bogota.sql)
-- ============================================

INSERT INTO zonas_destino (id, nombre, codigo, categoria, latitud_min, latitud_max, longitud_min, longitud_max, capacidad_max_paquetes, contador_paquetes, id_sede)
VALUES 
-- Zonas Medellín
(
    '660e8400-e29b-41d4-a716-446655440007',
    'Zona Norte Medellín',
    'ZD-MED-NORTE',
    'NORMAL',
    6.0,
    6.3,
    -75.6,
    -75.5,
    300,
    0,
    '550e8400-e29b-41d4-a716-446655440002'
),
(
    '660e8400-e29b-41d4-a716-446655440008',
    'Zona Sur Medellín',
    'ZD-MED-SUR',
    'NORMAL',
    6.1,
    6.2,
    -75.6,
    -75.5,
    300,
    0,
    '550e8400-e29b-41d4-a716-446655440002'
),
-- Zonas Cali
(
    '660e8400-e29b-41d4-a716-446655440009',
    'Zona Norte Cali',
    'ZD-CALI-NORTE',
    'NORMAL',
    3.4,
    3.6,
    -76.6,
    -76.5,
    400,
    0,
    '550e8400-e29b-41d4-a716-446655440003'
),
(
    '660e8400-e29b-41d4-a716-446655440010',
    'Zona Sur Cali',
    'ZD-CALI-SUR',
    'NORMAL',
    3.3,
    3.5,
    -76.6,
    -76.5,
    400,
    0,
    '550e8400-e29b-41d4-a716-446655440003'
);

-- ============================================
-- ZONAS DE ALMACENAJE
-- ============================================

INSERT INTO zonas_almacenaje (id, nombre, codigo, categoria, capacidad_max_kg, capacidad_max_m3, capacidad_max_paquetes, peso_actual_kg, volumen_actual_m3, contador_paquetes, estado, ubicacion_fisica, id_sede, zona_contingencia_id)
VALUES 
-- Zona Normal - Bogotá
(
    '770e8400-e29b-41d4-a716-446655440001',
    'Zona Normal A1',
    'ZA-NORMAL-A1',
    'NORMAL',
    2000.00,
    20.000,
    200,
    0.00,
    0.000,
    0,
    'DISPONIBLE',
    'Pasillo A, Estanteria 1',
    '550e8400-e29b-41d4-a716-446655440001',
    NULL
),
(
    '770e8400-e29b-41d4-a716-446655440002',
    'Zona Normal A2',
    'ZA-NORMAL-A2',
    'NORMAL',
    2000.00,
    20.000,
    200,
    0.00,
    0.000,
    0,
    'DISPONIBLE',
    'Pasillo A, Estanteria 2',
    '550e8400-e29b-41d4-a716-446655440001',
    NULL
),
-- Zona Delicada - Bogotá
(
    '770e8400-e29b-41d4-a716-446655440003',
    'Zona Delicada B1',
    'ZA-DELICADA-B1',
    'DELICADA',
    500.00,
    5.000,
    50,
    0.00,
    0.000,
    0,
    'DISPONIBLE',
    'Pasillo B, Estanteria 1',
    '550e8400-e29b-41d4-a716-446655440001',
    NULL
),
(
    '770e8400-e29b-41d4-a716-446655440004',
    'Zona Delicada B2',
    'ZA-DELICADA-B2',
    'DELICADA',
    500.00,
    5.000,
    50,
    0.00,
    0.000,
    0,
    'DISPONIBLE',
    'Pasillo B, Estanteria 2',
    '550e8400-e29b-41d4-a716-446655440001',
    NULL
),
-- Zona Alto Riesgo - Bogotá
(
    '770e8400-e29b-41d4-a716-446655440005',
    'Zona Alto Riesgo C1',
    'ZA-ALTO-RIESGO-C1',
    'ALTO_RIESGO',
    200.00,
    2.000,
    20,
    0.00,
    0.000,
    0,
    'DISPONIBLE',
    'Pasillo C, Estanteria 1',
    '550e8400-e29b-41d4-a716-446655440001',
    NULL
),
-- Zona Retencion - Bogotá
(
    '770e8400-e29b-41d4-a716-446655440006',
    'Zona Retencion D1',
    'ZA-RETENCION-D1',
    'RETENCION',
    100.00,
    1.000,
    10,
    0.00,
    0.000,
    0,
    'DISPONIBLE',
    'Pasillo D, Estanteria 1',
    '550e8400-e29b-41d4-a716-446655440001',
    NULL
),
-- Zonas Medellín
(
    '770e8400-e29b-41d4-a716-446655440007',
    'Zona Normal MED',
    'ZA-MED-NORMAL-01',
    'NORMAL',
    1500.00,
    15.000,
    150,
    0.00,
    0.000,
    0,
    'DISPONIBLE',
    'Pasillo 1',
    '550e8400-e29b-41d4-a716-446655440002',
    NULL
),
(
    '770e8400-e29b-41d4-a716-446655440008',
    'Zona Delicada MED',
    'ZA-MED-DELICADA-01',
    'DELICADA',
    300.00,
    3.000,
    30,
    0.00,
    0.000,
    0,
    'DISPONIBLE',
    'Pasillo 2',
    '550e8400-e29b-41d4-a716-446655440002',
    NULL
),
-- Zonas Cali
(
    '770e8400-e29b-41d4-a716-446655440009',
    'Zona Normal CALI',
    'ZA-CALI-NORMAL-01',
    'NORMAL',
    1800.00,
    18.000,
    180,
    0.00,
    0.000,
    0,
    'DISPONIBLE',
    'Pasillo 1',
    '550e8400-e29b-41d4-a716-446655440003',
    NULL
),
(
    '770e8400-e29b-41d4-a716-446655440010',
    'Zona Delicada CALI',
    'ZA-CALI-DELICADA-01',
    'DELICADA',
    400.00,
    4.000,
    40,
    0.00,
    0.000,
    0,
    'DISPONIBLE',
    'Pasillo 2',
    '550e8400-e29b-41d4-a716-446655440003',
    NULL
);
# 01 — Domain Core

## Glosario Técnico del Dominio

### Entidades del Negocio

| Entidad | Atributos Clave | Propósito |
|---|---|---|
| `Paquete` | id, fechaIngresoUtc, estado, sedeId, direccionDestino, coordenadas, estadoGps, valorDeclarado, metodoPago, remitente, destinatario, peso, dimensiones, volumenM3, pesoVolumetrico, pesoFacturable, tipoMercancia, categoriaCarga, indicadorFormaIrregular, precioEnvio, distanciaEstimadaKm, rutaId, zonaAlmacenamientoId, zonaDestinoId, alertaCargaEspecial, alertaDensidadAtipica, urlEvidenciaEntrega, nombreFirmante, fechaEntregaUtc, etiquetaDigital | Núcleo del sistema. Representa un envío desde su admisión hasta la entrega. |
| `ZonaAlmacenaje` | id, nombre, codigo, categoria, capacidadMaxKg, capacidadMaxM3, capacidadMaxPaquetes, pesoActualKg, volumenActualM3, contadorPaquetes, estado, ubicacionFisica, idSede, zonaContingenciaId | Almacenamiento físico en bodega. Controla capacidad por peso, volumen y cantidad. |
| `ZonaDestino` | id, nombre, codigo, categoria, latitudMin, latitudMax, longitudMin, longitudMax, capacidadMaxPaquetes, contadorPaquetes, idSede | Agrupación geográfica lógica para clasificar paquetes por destino. |
| `HistorialEstado` | id, paqueteId, estadoAnterior, estadoNuevo, observaciones, usuarioId, urlEvidencia, fechaTransicionUtc | Registro inmutable de cada transición de estado del paquete. |
| `Notificacion` | id, paqueteId, destinatario, tipo(SMS/EMAIL), mensaje, estado(PENDIENTE/ENVIADO/FALLIDO), fechaCreacionUtc, fechaEnvioUtc, intentos, mensajeError | Trazabilidad de notificaciones enviadas a remitente/destinatario. |
| `Persona` | tipoDocumento, numeroDocumento, nombreCompleto, telefono, correoElectronico, direccion | Embeddable reutilizado para remitente y destinatario. |
| `Sede` | id, nombre, direccion, tipo(PRINCIPAL/AUXILIAR), capacidadMaximaPeso, capacidadMaximaVolumen, tarifaBase, tarifaPorKg, tarifaPorKm, metodosPagoHabilitados | Punto de origen físico con configuración tarifaria. |
| `Usuario` | id, username, passwordHash, email, nombreCompleto, rol, enabled, fechaCreacion, fechaActualizacion | Seguridad y autenticación del sistema. |
| `EventoProcesado` | eventoId, paqueteId, tipoEvento, fechaProcesamientoUtc | Idempotencia: evita procesar eventos duplicados del Módulo de Rutas. |

### Value Objects

| Value Object | Tipo | Atributos | Validaciones / Reglas |
|---|---|---|---|
| `Peso` | Class | kilogramos: Double | `> 0` y `<= 70` kg |
| `Dimensiones` | Class | largoCm, anchoCm, altoCm: Double | Cada dimensión `> 0`. Método `calcularVolumenM3()` = (L×A×H)/1,000,000 |
| `Coordenadas` | Record | latitud: double, longitud: double | Latitud `[-90, 90]`, Longitud `[-180, 180]` |
| `Direccion` | Class (Embeddable) | direccion, ciudad, departamento, pais: String | Formato libre. Método `getDireccionCompleta()` |
| `PrecioEnvio` | Class | valor: BigDecimal | Simple wrapper sobre BigDecimal |
| `EstadoPaquete` | Enum | — | 10 estados: RECIBIDO_EN_SEDE, EN_CLASIFICACION, NOVEDAD_EN_BODEGA, LISTO_PARA_DESPACHO, EN_TRANSITO, EN_PARADA_DE_ENTREGA, ENTREGADO, DEVOLUCION_EN_RUTA, EXTRAVIADO_EN_RUTA, DAÑADO_EN_RUTA |
| `EstadoZona` | Enum | — | DISPONIBLE, PARCIAL, SATURADO, BLOQUEADO |
| `EstadoGps` | Enum | — | PENDIENTE, RESUELTO |
| `CategoriaCarga` | Enum | — | NORMAL, CARGA_ESPECIAL |
| `CategoriaZona` | Enum | — | NORMAL, DELICADA, ALTO_RIESGO, RETENCION |
| `TipoMercancia` | Enum | — | ESTANDAR, FRAGIL, PELIGROSO |
| `TipoNovedad` | Enum | — | DAÑADO, EXTRAVIADO |
| `TipoDocumento` | Enum | — | CEDULA_CIUDADANIA, CEDULA_EXTRANJERIA, PASAPORTE, NIT |
| `TipoSede` | Enum | — | PRINCIPAL, AUXILIAR |
| `MetodoPago` | Enum | — | PREPAGO, CONTRA_ENTREGA |

## Reglas de Negocio Puras y Validaciones Intrínsecas

### Reglas Físicas del Paquete (`Paquete.procesarPesaje()`)

| ID | Regla | Aplica en |
|---|---|---|
| FR-001 | Peso debe ser `> 0` y `<= 70` kg | `Peso` constructor |
| FR-002 | Carga Especial si `peso > 50` kg OR `volumen > 0.5` m³ (y dentro de rangos permitidos) | `Paquete.determinarCategoriaCarga()` |
| FR-003 | Cada dimensión (largo, ancho, alto) debe ser `> 0` | `Dimensiones` constructor |
| FR-004 | Volumen = `(L × A × H) / 1,000,000` (m³) | `Dimensiones.calcularVolumenM3()` |
| FR-005 | Peso Volumétrico = `Volumen m³ × 250 kg/m³` | `Paquete.calcularPesoVolumetrico()` |
| FR-006 | Peso Facturable = `MAX(Peso Real, Peso Volumétrico)` | `Paquete.determinarPesoFacturable()` |
| FR-010 | Densidad Atípica si `|Peso Real - Peso Volumétrico| / Peso Real > 30%` | `Paquete.verificarDensidadAtipica()` |

### Reglas de Transición de Estado (`EstadoPaquete.esTransicionValidaDesde()`)

```
ENTREGADO → (ninguno) — Bloqueado
EN_TRANSITO ← LISTO_PARA_DESPACHO
EN_PARADA_DE_ENTREGA ← EN_TRANSITO
ENTREGADO ← EN_PARADA_DE_ENTREGA
DEVOLUCION_EN_RUTA ← EN_TRANSITO | EN_PARADA_DE_ENTREGA
EXTRAVIADO_EN_RUTA ← EN_TRANSITO | EN_PARADA_DE_ENTREGA
DAÑADO_EN_RUTA ← EN_TRANSITO | EN_PARADA_DE_ENTREGA
```

### Matriz Completa de Transiciones de Estado

| Estado Actual | → Siguiente | Trigger | Endpoint |
|---|---|---|---|
| `RECIBIDO_EN_SEDE` | `EN_CLASIFICACION` | Asignar zona almacenamiento | POST `/api/paquetes/{id}/almacenaje` |
| `RECIBIDO_EN_SEDE` | `NOVEDAD_EN_BODEGA` | Registrar novedad | POST `/api/paquetes/{id}/novedades` |
| `EN_CLASIFICACION` | `LISTO_PARA_DESPACHO` | Confirmar clasificación | POST `/api/paquetes/clasificacion/confirmar` |
| `EN_CLASIFICACION` | `NOVEDAD_EN_BODEGA` | Registrar novedad | POST `/api/paquetes/{id}/novedades` |
| `LISTO_PARA_DESPACHO` | `EN_TRANSITO` | M2 vía `eventos-paquete-queue`: `PAQUETE_EN_TRANSITO` | — |
| `EN_TRANSITO` | `EN_PARADA_DE_ENTREGA` | M2 vía `eventos-paquete-queue`: `PARADA_FALLIDA` (con reintento) | — |
| `EN_TRANSITO` | `DEVOLUCION_EN_RUTA` | M2 vía `eventos-paquete-queue`: `PARADA_FALLIDA` / `PARADAS_SIN_GESTIONAR` / `PAQUETE_EXCLUIDO_DESPACHO` | — |
| `EN_TRANSITO` | `EXTRAVIADO_EN_RUTA` | M2 vía `eventos-paquete-queue`: `NOVEDAD_GRAVE` (`tipo_novedad: EXTRAVIADO`) | — |
| `EN_TRANSITO` | `DAÑADO_EN_RUTA` | M2 vía `eventos-paquete-queue`: `NOVEDAD_GRAVE` (`tipo_novedad: DAÑADO_EN_RUTA`) | — |
| `EN_PARADA_DE_ENTREGA` | `ENTREGADO` | M2 vía `eventos-paquete-queue`: `PAQUETE_ENTREGADO` | — |
| `EN_PARADA_DE_ENTREGA` | `DEVOLUCION_EN_RUTA` | M2 vía `eventos-paquete-queue`: `NOVEDAD_GRAVE` (`tipo_novedad: DEVOLUCION`) | — |

### Reglas de Novedades en Bodega

| Condición | Validación |
|---|---|
| Estados permitidos para novedad | Solo `RECIBIDO_EN_SEDE` o `EN_CLASIFICACION` |
| Evidencia para DAÑADO | URL de evidencia obligatoria (`EvidenciaRequeridaException`) |
| Evidencia para EXTRAVIADO | No requiere evidencia |

### Reglas de Zona de Almacenaje

| Regla | Descripción |
|---|---|
| Compatibilidad | `CategoriaZona.esCompatible(TipoMercancia)`: PELIGROSO→ALTO_RIESGO, FRAGIL→DELICADA\|ALTO_RIESGO, ESTANDAR→NORMAL\|DELICADA\|ALTO_RIESGO |

**Matriz de Compatibilidad Zona-Mercancía:**

| Tipo Mercancía | `NORMAL` | `DELICADA` | `ALTO_RIESGO` | `RETENCION` |
|---|---|---|---|---|
| `ESTANDAR` | ✅ | ✅ | ✅ | ❌ |
| `FRAGIL` | ❌ | ✅ | ✅ | ❌ |
| `PELIGROSO` | ❌ | ❌ | ✅ | ❌ |
| Saturación | Se lanza `ZonaSaturadaException` si se excede `capacidadMaxKg`, `capacidadMaxM3` o `capacidadMaxPaquetes` |
| Estado por ocupación | `< 70%` = DISPONIBLE, `70-90%` = PARCIAL, `> 90%` = SATURADO |

### Reglas de Zona de Destino

| Regla | Descripción |
|---|---|
| Pertinencia geográfica | `ZonaDestino.contieneCoordenas()` valida si lat/lon están dentro de los límites |
| Apta para mercancía | `ZonaDestino.esAptaPara()`: misma compatibilidad que CategoriaZona |
| Capacidad | `ZonaDestino.tieneCapacidadDisponible()`: contador < capacidad máxima |

### Reglas de Precio de Envío (`PriceCalculationServiceImpl`)

| Componente | Fórmula |
|---|---|
| Tarifa Base | \$5,000 COP fijo |
| Costo Peso | `pesoFacturable × \$1,500/kg` |
| Costo Distancia | `distanciaKm × \$50/km` |
| Recargo Peligroso | +\$5,000 |
| Recargo Frágil | +\$3,000 |
| Recargo Carga Especial | +\$8,000 |
| Recargo Forma Irregular | +\$2,000 |
| Recargo Densidad Atípica | +\$1,500 |
| IVA | 19% sobre subtotal |

## Excepciones Personalizadas de Negocio (15)

| Excepción | Dispara cuando |
|---|---|
| `CoordenadasInvalidasException` | Latitud fuera [-90,90] o longitud fuera [-180,180] |
| `DistanciaRequeridaException` | Se intenta calcular precio sin distancia estimada |
| `EstadoTransicionInvalidaException` | Transición de estado no válida (ej: ENTREGADO→EN_TRANSITO) |
| `EventoDuplicadoException` | Evento de ruta ya procesado (idempotencia) |
| `EvidenciaRequeridaException` | Novedad DAÑADO sin archivo de evidencia |
| `InvalidCoverageException` | Dirección de destino fuera del área de cobertura |
| `PaqueteNotFoundException` | Paquete no encontrado por ID |
| `TimeoutGeocodingException` | Timeout en llamada a API de geocodificación |
| `ZonaAlmacenajeNotFoundException` | Zona de almacenaje no encontrada por ID |
| `ZonaDestinoNoEncontradaException` | No hay zona de destino para coordenadas dadas |
| `ZonaDestinoNotFoundException` | Zona de destino no encontrada por ID |
| `ZonaDestinoSaturadaException` | Zona de destino sin capacidad disponible |
| `ZonaIncompatibleException` | Tipo de mercancía incompatible con categoría de zona de almacenaje |
| `ZonaNoAptaException` | Tipo de mercancía incompatible con zona de destino |
| `ZonaSaturadaException` | Zona de almacenaje sin capacidad (peso, volumen o paquetes) |

## Servicio de Dominio

| Servicio | Método | Dependencia (Puerto) | Lógica |
|---|---|---|---|
| `CalculoZonaDestinoService` | `calcularZona(Paquete)` → `ZonaDestino` | `ZonaDestinoRepository.findAllActivas()` | Busca la primera zona activa cuyas coordenadas contengan las coordenadas del paquete. Lanza `ZonaDestinoNoEncontradaException` si no hay match. |

## Evento de Dominio

| Evento | Atributos | Propósito |
|---|---|---|
| `SolicitudRutaEvent` | paqueteId, timestamp | Dispara solicitud de ruta después del pesaje exitoso |

## Flujos Completos del Dominio

### Flujo Normal Completo
```
1. POST /api/paquetes/admision
   └─→ Estado: RECIBIDO_EN_SEDE
2. POST /api/paquetes/pesaje
   └─→ Sin cambio de estado
3. POST /api/paquetes/{id}/almacenaje
   └─→ Estado: EN_CLASIFICACION
4. GET /api/paquetes/clasificacion/sugerencia/{id}
   └─→ Retorna zona sugerida
5. POST /api/paquetes/clasificacion/confirmar
   └─→ Estado: LISTO_PARA_DESPACHO
6. [Módulo Rutas inicia ruta]
   └─→ Estado: EN_TRANSITO → EN_PARADA_DE_ENTREGA → ENTREGADO
```

### Flujo con Novedad en Bodega
```
1. POST /api/paquetes/admision → RECIBIDO_EN_SEDE
2. POST /api/paquetes/{id}/novedades (con evidencia si DAÑADO) → NOVEDAD_EN_BODEGA
```

### Flujo con Discrepancia Física
```
1. POST /api/paquetes/admision → RECIBIDO_EN_SEDE
2. POST /api/paquetes/{id}/almacenaje (con datosDiscrepancia)
   └─→ EN_CLASIFICACION + datos actualizados
3. POST /api/paquetes/clasificacion/confirmar → LISTO_PARA_DESPACHO
```

### Casos Especiales

| Caso | Descripción | Restricción |
|---|---|---|
| Forma Irregular | `indicadorFormaIrregular: true` | Afecta cálculo de volumen |
| Mercancía Peligrosa | `tipoMercancia: PELIGROSO` | Solo zonas `ALTO_RIESGO` |
| Contra Entrega | `metodoPago: CONTRA_ENTREGA` | Valor declarado afecta precio |

---

---

---

---

---

---

## Anexo: Estado de Archivos Actual (domain)
*Generado automáticamente por sync-agent-docs.py el 2026-05-25 23:27:36 UTC*

| Indicador | Valor |
|---|---|
| Clases | 30 |
| Interfaces | 0 |
| Enumeraciones | 11 |
| Records | 1 |
| Métodos públicos (significativos) | 37 |
| Archivos analizados | 42 |

### Tipos Detectados

| Tipo | Nombre | Paquete | Métodos públicos |
|---|---|---|---|
| 🟦 Cls | `SolicitudRutaEvent` | `com.logistics.packages.domain.event` | `—` |
| 🟦 Cls | `CoordenadasInvalidasException` | `com.logistics.packages.domain.exception` | `—` |
| 🟦 Cls | `DistanciaRequeridaException` | `com.logistics.packages.domain.exception` | `—` |
| 🟦 Cls | `EstadoTransicionInvalidaException` | `com.logistics.packages.domain.exception` | `—` |
| 🟦 Cls | `EventoDuplicadoException` | `com.logistics.packages.domain.exception` | `—` |
| 🟦 Cls | `EvidenciaRequeridaException` | `com.logistics.packages.domain.exception` | `—` |
| 🟦 Cls | `InvalidCoverageException` | `com.logistics.packages.domain.exception` | `—` |
| 🟦 Cls | `PaqueteNotFoundException` | `com.logistics.packages.domain.exception` | `—` |
| 🟦 Cls | `TimeoutGeocodingException` | `com.logistics.packages.domain.exception` | `—` |
| 🟦 Cls | `ZonaAlmacenajeNotFoundException` | `com.logistics.packages.domain.exception` | `—` |
| 🟦 Cls | `ZonaDestinoNoEncontradaException` | `com.logistics.packages.domain.exception` | `—` |
| 🟦 Cls | `ZonaDestinoNotFoundException` | `com.logistics.packages.domain.exception` | `—` |
| 🟦 Cls | `ZonaDestinoSaturadaException` | `com.logistics.packages.domain.exception` | `—` |
| 🟦 Cls | `ZonaIncompatibleException` | `com.logistics.packages.domain.exception` | `—` |
| 🟦 Cls | `ZonaNoAptaException` | `com.logistics.packages.domain.exception` | `—` |
| 🟦 Cls | `ZonaSaturadaException` | `com.logistics.packages.domain.exception` | `—` |
| 🟦 Cls | `EventoProcesado` | `com.logistics.packages.domain.model` | `—` |
| 🟦 Cls | `HistorialEstado` | `com.logistics.packages.domain.model` | `—` |
| 🟨 Enm | `TipoNotificacion` | `com.logistics.packages.domain.model` | `marcarComoEnviada, marcarComoFallida` |
| 🟦 Cls | `Paquete` | `com.logistics.packages.domain.model` | `crearNuevo, reconstruir, asignarCoordenadas, asignarPrecio, asignarPrecioEnvio, procesarPesaje, c...` |
| 🟦 Cls | `Persona` | `com.logistics.packages.domain.model` | `—` |
| 🟦 Cls | `Sede` | `com.logistics.packages.domain.model` | `validarMetodoPagoSoportado, tieneCapacidadPara` |
| 🟦 Cls | `Usuario` | `com.logistics.packages.domain.model` | `—` |
| 🟦 Cls | `ZonaAlmacenaje` | `com.logistics.packages.domain.model` | `puedeAlbergar, agregarPaquete, tieneCapacidadPara` |
| 🟦 Cls | `ZonaDestino` | `com.logistics.packages.domain.model` | `esAptaPara, tieneCapacidadDisponible, incrementarContador, contieneCoordenas` |
| 🟦 Cls | `CalculoZonaDestinoService` | `com.logistics.packages.domain.service` | `calcularZona` |
| 🟨 Enm | `CategoriaCarga` | `com.logistics.packages.domain.valueobject` | `—` |
| 🟨 Enm | `CategoriaZona` | `com.logistics.packages.domain.valueobject` | `esCompatible` |
| 🟪 Rec | `Coordenadas` | `com.logistics.packages.domain.valueobject` | `—` |
| 🟦 Cls | `Dimensiones` | `com.logistics.packages.domain.valueobject` | `calcularVolumenM3` |
| 🟦 Cls | `Direccion` | `com.logistics.packages.domain.valueobject` | `—` |
| 🟨 Enm | `EstadoGps` | `com.logistics.packages.domain.valueobject` | `—` |
| 🟨 Enm | `EstadoPaquete` | `com.logistics.packages.domain.valueobject` | `permiteNovedadEnBodega, esTransicionValidaDesde` |
| 🟨 Enm | `EstadoZona` | `com.logistics.packages.domain.valueobject` | `—` |
| 🟨 Enm | `MetodoPago` | `com.logistics.packages.domain.valueobject` | `—` |
| 🟦 Cls | `NovedadBodega` | `com.logistics.packages.domain.valueobject` | `—` |
| 🟦 Cls | `Peso` | `com.logistics.packages.domain.valueobject` | `—` |
| 🟦 Cls | `PrecioEnvio` | `com.logistics.packages.domain.valueobject` | `—` |
| 🟨 Enm | `TipoDocumento` | `com.logistics.packages.domain.valueobject` | `—` |
| 🟨 Enm | `TipoMercancia` | `com.logistics.packages.domain.valueobject` | `—` |
| 🟨 Enm | `TipoNovedad` | `com.logistics.packages.domain.valueobject` | `—` |
| 🟨 Enm | `TipoSede` | `com.logistics.packages.domain.valueobject` | `—` |

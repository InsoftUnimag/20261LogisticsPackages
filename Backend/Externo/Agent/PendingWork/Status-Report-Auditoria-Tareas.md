# Reporte de Estado del Proyecto — Auditoría vs Task.md

**Fecha:** 2026-05-23  
**Última actualización:** 2026-05-23 (post Bloque 2: feature/m3-async-sqs-migration)  
**Auditor:** Agente de Revisión Arquitectónica  
**Documento fuente:** `Backend/Externo/Agent/PendingWork/Task.md`

---

## Resumen Ejecutivo

| Tarea | Estado | Progreso Estimado |
|-------|--------|:------------------:|
| **1.** Migración RabbitMQ → Amazon SQS | ✅ **Completada** | 100% |
| **2.** Definición/Diseño flujo M2 (Contratos e Infraestructura) | ✅ **Completada** | 100% |
| **3.** Implementación secuencial de las 3 colas de M2 | ✅ **Completada** | ~100% |
| **4.** Actualización UC/IP 007 (Gestión Novedades — Síncrono a Asíncrono) | ✅ **Completada** | 100% |
| **5.** Verificación y Pruebas de Humo en AWS Real | ❌ **No iniciada** | 0% |
| **6.** Contextualización sobre Módulo Profesor | ❌ **No iniciada** | 0% |
| **7.** JSON para Módulo Profesor | ❌ **No iniciada** | 0% |

### PRs ejecutados en esta iteración

| PR | Alcance | Rama | Estado |
|----|---------|------|--------|
| PR1 | Migrar `AdmisionController` a `@Builder` para `ConsultaPaqueteResponse` | — | ✅ Commiteado |
| PR2 | Renombrar `ConsultaPaqueteResponse` → `GestionNovedadPaqueteResponse` + corrección documentación | — | ✅ Commiteado |
| PR3 | Eliminar `RutaEventAdapter`, `RutaEventPublisher` (ambos paquetes) y propiedad obsoleta | — | ✅ Commiteado |
| PR4 | Alineación infraestructura SQS: eliminar DEV_PREFIX, crear perfil aws, añadir @Profile a listeners | `feature/sqs-infrastructure-alignment` | ✅ Commiteado |
| PR5 | Corrección nombre cola logistics-eventos-paquete según ARN de M2 | `fix/m2-queue-name-arn` | ✅ Commiteado |
| PR6 | Alinear contratos de salida M1→M2: Instant + String en SolicitudRutaPayload | `bugfix/m2-outbound-contracts` | ✅ Commiteado |
| PR7 | Alinear contratos de entrada M2→M1: Instant en DTOs, fallback enums, fechaHoraEvento en mapper | `bugfix/m2-inbound-contracts` | ✅ Commiteado |
| PR8 | Purificación del dominio: eliminar @Setter, @Embeddable, NovedadBodega VO, Paquete.reconstruir() | `bugfix/domain-immutability-and-states` | ✅ Commiteado |
| PR9 | Migración M3 asíncrona: eliminar sync controller/UC, crear puerto/adapter/DTO SQS, integrar en UC, tests + Bloque 2 (catch→throw adapter, tests RegistrarNovedadUseCase, test propagación excepción) | `feature/m3-async-sqs-migration` | ✅ Commiteado |

---

## Tarea 1 — Migración de RabbitMQ a Amazon SQS ✅ COMPLETADA

### Estado: 100% — Sin observaciones

| Componente | Estado | Archivo |
|------------|--------|---------|
| Dependencia Spring Cloud AWS SQS 3.1.1 | ✅ | `build.gradle` |
| Adaptador productor Cola 1 (SOLICITAR_RUTA) | ✅ | `infrastructure/adapter/messaging/RutaSqsAdapter.java` |
| Adaptador consumidor Cola 2 (RUTA_ASIGNADA) | ✅ | `infrastructure/adapter/messaging/RutaSqsListener.java` |
| Adaptador consumidor Cola 3 (Eventos M2) | ✅ | `infrastructure/adapter/messaging/RutaEventSqsListener.java` |
| Adaptador cola clasificación | ✅ | `infrastructure/adapter/messaging/PaqueteListoClasificacionSqsListener.java` |
| Adaptador publicación novedades | ✅ | `infrastructure/adapter/messaging/NovedadEventAdapter.java` |
| Adaptador publicación clasificación | ✅ | `infrastructure/adapter/messaging/ClasificacionEventAdapter.java` |
| Antiguo adaptador RabbitMQ deprecado | ❌ **Eliminado en PR3** | ~~`RutaEventAdapter.java`~~ |
| Interfaz deprecada `RutaEventPublisher` (app.repository) | ❌ **Eliminado en PR3** | ~~`RutaEventPublisher.java`~~ |
| Interfaz huérfana `RutaEventPublisher` (app.ports) | ❌ **Eliminado en PR3** | ~~`RutaEventPublisher.java`~~ |
| LocalStack para desarrollo | ✅ | `docker-compose.yml` (servicio `localstack:3.4`) |
| Colas con nombres fijos (sin prefijo) | ✅ | `application.yml` y `application-local.yml` — `${DEV_PREFIX}` eliminado, nombres exactos por contrato M2 |

> [!NOTE]
> Con la eliminación de `RutaEventAdapter` en PR3, ya no existe código legacy del patrón RabbitMQ. La propiedad `aws.sqs.ruta-event-queue` también fue eliminada de `application.properties`.

---

## Tarea 2 — Definición y Diseño del flujo con M2 ✅ COMPLETADA (100%)

### GitFlow — ✅ Completo

- Ramas: `master`, `develop`, `feature/*`, `bugfix/*`, `fix/*`, `chore/*`
- Documentos de estrategia: `CommitsAndPRGeneration.md`, `ConsideracionesAgentes.md`
- Flujo de integración: Pull Requests hacia `develop`, merges periódicos
- Perfiles SQS alineados con M2: `default`/`local` → LocalStack, `aws` → AWS real
- Listeners protegidos con `@Profile({"default", "local", "aws"})`

### Mapeo de DTOs con M2 — ✅ Discrepancias de Contrato Resueltas

#### ✅ CONFIRMADO — `direccion`: Objeto válido para M2

| Lado | Tipo | Valor |
|------|------|-------|
| **M1** (actual) | `SolicitudRutaPayload.DireccionDto` | Objeto `{direccion, ciudad, pais}` |
| **M2** (esperado) | Objeto (DTO anidado) | M2 acepta objeto dirección, no requiere String plano |
| **Archivo** | `infrastructure/dto/request/SolicitudRutaPayload.java` | Línea 18 |
| **Estado** | — | ✅ **Compatible** — M2 recibe el objeto sin problema |

> M2 acepta `direccion` como objeto estructurado, no requiere concatenación a String plano.

#### ✅ RESUELTO — `fecha_limite_entrega`: OffsetDateTime → Instant (OUTBOUND)

| Lado | Tipo | Archivo |
|------|------|---------|
| **M1** (antes) | `OffsetDateTime` + `@JsonFormat` | `SolicitudRutaPayload.java:42-44` |
| **M1** (después) | `Instant` | `SolicitudRutaPayload.java:39-40` |
| **M2** (esperado) | `Instant` | `SolicitarRutaRequest` |

**Solución:** Se cambió el tipo a `Instant` en el DTO y se agregó `.toInstant()` en `RutaSqsAdapter.calcularFechaLimite()`. Commit `3932d8f`.

#### ✅ RESUELTO — Enums `TipoMercancia` / `MetodoPago`: Enum → String

| Lado | Tipo | Archivo |
|------|------|---------|
| **M1** (antes) | `TipoMercancia`, `MetodoPago` (enums de dominio) | `SolicitudRutaPayload.java:46-50` |
| **M1** (después) | `String` | `SolicitudRutaPayload.java:42-46` |
| **M2** (esperado) | `String` | `SolicitarRutaRequest` |

**Solución:** Se cambió el tipo a `String` en el DTO y se agregó `.name()` en `RutaSqsAdapter.mapToPayload()`. Commits `3932d8f` + `3b52285`.

#### ✅ RESUELTO — `fecha_hora_evento`: OffsetDateTime → Instant (INBOUND)

| Lado | Tipo | Archivo |
|------|------|---------|
| **M1** (antes) | `OffsetDateTime` + `@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")` | `EventoPaqueteM2Dto.java:44-45` |
| **M1** (después) | `Instant` | `EventoPaqueteM2Dto.java:42-43` |
| **M2** (real) | `Instant` | `PaqueteEnTransitoEvent`, `PaqueteEntregadoEvent`, etc. |

**Solución:** Se cambió el tipo a `Instant` en los 7 DTOs y se eliminó `@JsonFormat`. El mapper convierte `Instant.atOffset(ZoneOffset.UTC)` a `OffsetDateTime` y lo asigna a `EventoRutaDto.fechaHoraEvento`. PR7.

#### ⚠️ ALTA 3 — Flujo `RUTA_ASIGNADA` no implementado en M2

| Componente | Estado |
|------------|--------|
| Consumidor M1 (`RutaSqsListener`) | ✅ Listo, escucha `respuestas-ruta-queue` |
| Productor M2 | ❌ **No implementado** — M2 no envía evento `RUTA_ASIGNADA` |

**Impacto:** Los paquetes nunca pasarán a `LISTO_PARA_DESPACHO` de forma asíncrona.

#### ⚠️ MEDIA 4 — Otras discrepancias menores

| Hallazgo | Detalle | Archivo |
|----------|---------|---------|
| `volumen_m3` | M1 lo envía, M2 no lo espera | `SolicitudRutaPayload.java` |
| ~~Enums M1 vs Strings M2~~ | ~~`TipoMercancia`, `MetodoPago` (enums M1) vs `String` (M2)~~ | ~~**Resuelto en PR6**~~ |
| ~~`MotivoParadaFallida` (enum M1) vs `String` (M2)~~ | ~~Posible error de deserialización~~ | ~~**Resuelto en PR7** — Campo `String` + fallback `mapMotivo()`~~ |
| ~~`TipoNovedadGrave` (enum M1) vs `String` (M2)~~ | ~~Posible error de deserialización~~ | ~~**Resuelto en PR7** — Campo `String` + fallback `mapTipoNovedad()`~~ |

---

## Tarea 3 — Implementación secuencial de las 3 colas de M2 ⚠️ COMPLETADA CON RIESGOS (90%)

### Cola 1 — `SOLICITAR_RUTA` (M1 escribe / M2 lee) ✅

| Aspecto | Estado |
|---------|--------|
| Producer M1 | ✅ `RutaSqsAdapter` → `solicitudes-ruta-queue` |
| Payload | ✅ `SolicitudRutaPayload` con campos completos |
| Contrato | ✅ `fecha_limite_entrega` como `Instant`, enums como `String` — **Resuelto en PR6** |

### Cola 2 — `respuestas-ruta-queue` (M1 lee) ⚠️

| Aspecto | Estado |
|---------|--------|
| Consumer M1 | ✅ `RutaSqsListener` escucha `respuestas-ruta-queue` |
| Producer M2 | ❌ **No implementado** — M2 no produce `RUTA_ASIGNADA` |
| ⚠️ Riesgo | Flujo asíncrono incompleto. Paquetes no avanzan a `LISTO_PARA_DESPACHO` |

### Cola 3 — `eventos-paquete-queue` (M1 lee) ✅

| Aspecto | Estado |
|---------|--------|
| Consumer M1 | ✅ `RutaEventSqsListener` escucha `logistics-eventos-paquete` (ARN M2) |
| Deserialización polimórfica | ✅ `@JsonTypeInfo` + `@JsonSubTypes` (6 subtipos) |
| Mapper | ✅ `EventoPaqueteM2Mapper` — cubre los 6 tipos de eventos, con fallback para enums |
| Contrato de fecha | ✅ `Instant` en DTOs, conversión nativa Jackson, mapper convierte a `OffsetDateTime` |
| Enums | ✅ Campos como `String` con validación + fallback en mapper |

### Mapa de interacciones actual

```
M1 (Packages)                              M2 (Routes)
┌──────────────────┐                    ┌───────────────────┐
│ RutaSqsAdapter   │──SOLICITAR_RUTA──▶ │ SolicitarRuta    │
│ (Producer)       │   solicitudes-     │ Consumer         │
│                  │   ruta-queue       │                  │
│ RutaEventSqs     │◀─eventos-paquete──│ SqsIntegracion   │
│ Listener         │   6 tipos eventos  │ Modulo1Adapter   │
│ (Consumer)       │                    │ (Producer)       │
│                  │                    │                  │
│ RutaSqsListener  │◀──???─NO IMPL───  │ ???              │
│ (Consumer)       │   respuestas-      │                  │
│                  │   ruta-queue       │                  │
└──────────────────┘                    └──────────────────┘
```

---

## Tarea 4 — Migración M3 (Finanzas) Síncrono → Asíncrono vía SQS ✅ COMPLETADA (100%)

### Componentes síncronos eliminados — ❌

| Componente | Archivo | Acción |
|------------|---------|--------|
| `ConsultaFinancieraController` | `infrastructure/controller/ConsultaFinancieraController.java` | ❌ **Eliminado** — Endpoint REST síncrono `GET /route/{idRoute}/package/{idPaquete}` |
| `ConsultarEstadoPaqueteUseCase` | `application/usecase/gestionnovedad/ConsultarEstadoPaqueteUseCase.java` | ❌ **Eliminado** — Caso de uso para consulta síncrona a M3 |
| `GestionNovedadPaqueteResponse` | `application/usecase/gestionnovedad/GestionNovedadPaqueteResponse.java` | ❌ **Eliminado** — DTO de respuesta financiera (renombrado en PR2) |

### Componentes asíncronos creados — ✅

| Componente | Archivo | Funcionalidad |
|------------|---------|---------------|
| `EstadoPaqueteFinanzasPublisher` | `application/ports/EstadoPaqueteFinanzasPublisher.java` | Puerto hexagonal: `publicarEstadoFinal(Paquete paquete)` |
| `EventoFinancieroPaqueteDto` | `infrastructure/dto/event/EventoFinancieroPaqueteDto.java` | DTO con `@JsonProperty` snake_case (`id_paquete`, `id_ruta`, `estado`) |
| `FinanzasEventSqsAdapter` | `infrastructure/adapter/messaging/FinanzasEventSqsAdapter.java` | Adaptador SQS que implementa el puerto usando `SqsTemplate` |
| `ProcesarEventoRutaUseCase` (modificado) | `application/usecase/gestionnovedad/ProcesarEventoRutaUseCase.java` | Invoca publisher tras notificaciones (eventos M2) |
| `RegistrarNovedadUseCase` (modificado) | `application/usecase/novedad/RegistrarNovedadUseCase.java` | Invoca publisher tras evento interno de novedad |
| `FinanzasEventSqsAdapterTest` | `test/.../FinanzasEventSqsAdapterTest.java` | 6 tests: ENTREGADO, NOVEDAD_EN_BODEGA, EN_TRANSITO, rutaId null, propagación de excepción SQS |
| `ProcesarEventoRutaUseCaseTest` (modificado) | `test/.../ProcesarEventoRutaUseCaseTest.java` | Verificaciones `verify`/`never` para publicación M3 |
| `RegistrarNovedadUseCaseTest` (modificado) | `test/.../RegistrarNovedadUseCaseTest.java` | Mock + verificaciones `verify`/`never` para `EstadoPaqueteFinanzasPublisher` añadidas en 5 tests |

### Componentes existentes relevantes — ✅

| Componente | Archivo | Funcionalidad |
|------------|---------|---------------|
| `EventoRutaDto` | `application/usecase/gestionnovedad/EventoRutaDto.java` | DTO con 6 `TipoEventoRuta` |
| `RutaEventSqsListener` | `infrastructure/adapter/messaging/RutaEventSqsListener.java` | Consumo asíncrono de eventos M2 |
| `EventoPaqueteM2Mapper` | `infrastructure/adapter/messaging/EventoPaqueteM2Mapper.java` | Mapeo M2 DTOs → `EventoRutaDto` |
| Idempotencia (FR-008) | `domain/model/EventoProcesado.java` + Flyway V5 | Tabla `eventos_procesados` con validación |
| Notificaciones (FR-002) | `application/ports/NotificacionPort.java` + `MockNotificacionAdapter` | SMS + Email con manejo de fallos |
| 6 transiciones de estado en `Paquete.java` | `domain/model/Paquete.java` | `transitarAEnRuta()`, `entregarPaquete()`, `registrarDevolucionEnRuta()`, etc. |

### Observaciones — ⚠️

| # | Hallazgo | Severidad | Detalle | Estado |
|---|----------|-----------|---------|--------|
| 1 | **`@Setter` a nivel de clase en `Paquete.java`** | 🔴 **ALTA** | Viola el principio de inmutabilidad del dominio. Cualquier código puede mutar el estado sin pasar por métodos de negocio. Documentado en `ConsideracionesAgentes.md` | ✅ **Resuelto en PR8** — Eliminado `@Setter` class-level y field `etiquetaDigital`. Creadas fábricas `crearNuevo()` y `reconstruir()`. `@NoArgsConstructor` privado. |
| 2 | **`EN_PARADA_DE_ENTREGA` sin evento M2 correspondiente** | ⚠️ MEDIA | El `ProcesarEventoRutaUseCase` lo maneja, pero M2 no tiene evento equivalente. El mapper nunca generará este tipo. | ❌ Pendiente |
| 3 | **Mapper no recibía `fechaHoraEvento`** | ⚠️ MEDIA | ~~En `EventoPaqueteM2Mapper.construirDto()`, `fechaHoraEvento` no se usaba para construir el `EventoRutaDto` (línea 74-90), solo para generar el `eventoId`~~ | ✅ **Resuelto en PR7** — Ahora se asigna `fechaHoraEvento` al DTO |
| 4 | **`registrarNovedad()` usa `NOVEDAD_EN_BODEGA` fijo** | ⚠️ MEDIA | `Paquete.registrarNovedad()` cambia siempre a `NOVEDAD_EN_BODEGA` sin distinguir subtipos de novedad | ✅ **Resuelto en PR8** — Ahora recibe `NovedadBodega` VO que preserva `tipoNovedad` en `HistorialEstado` (columna `tipo_novedad` + Flyway V7). La transición de estado sigue siendo `NOVEDAD_EN_BODEGA` pero el contexto completo del subtipo queda registrado en el historial. |
| 5 | **Migración M3: endpoint REST síncrono eliminado** | ✅ COMPLETADA | Se eliminó `ConsultaFinancieraController`, `ConsultarEstadoPaqueteUseCase` y `GestionNovedadPaqueteResponse`. Se creó puerto `EstadoPaqueteFinanzasPublisher`, adaptador `FinanzasEventSqsAdapter` y DTO `EventoFinancieroPaqueteDto`. | ✅ **Completado en PR9** |
| 6 | **Publisher M3 no integrado en casos de uso** | 🔴 ALTA | ~~El adaptador SQS existe pero no se invoca desde ningún caso de uso~~ | ✅ **Resuelto en PR9** — Integrado en `ProcesarEventoRutaUseCase` y `RegistrarNovedadUseCase` |
| 7 | **Payload SQS mínimo (3 campos)** | ℹ️ INFORMATIVO | El DTO `EventoFinancieroPaqueteDto` solo envía `id_paquete`, `id_ruta`, `estado` con snake_case. M3 consulta detalles adicionales por su cuenta. | ✅ Diseño intencional |
| 8 | **Tests de integración SQS M3 ausentes** | ⚠️ MEDIA | ~~No hay tests para el adaptador SQS de M3~~ | ✅ **Resuelto en PR9** — `FinanzasEventSqsAdapterTest` con 6 escenarios |
| 9 | **Adapter SQS M3 traga excepción (catch sin throw)** | 🔴 ALTA | `FinanzasEventSqsAdapter` capturaba la excepción sin relanzarla, impidiendo el rollback transaccional cuando SQS falla. Contradice la decisión arquitectónica de "es preferible no actualizar el estado si no se puede notificar a Finanzas" | ✅ **Resuelto en Bloque 2** — catch→throw; se agrega test `testPropagarExcepcionCuandoSqsFalla` |
| 10 | **RegistrarNovedadUseCaseTest sin mock de FinanzasPublisher** | 🔴 ALTA | `RegistrarNovedadUseCaseTest` no tenía `@Mock` para `EstadoPaqueteFinanzasPublisher`, causando NPE al ejecutar `registrarNovedad()` | ✅ **Resuelto en Bloque 2** — Se añadió mock y aserciones `verify`/`never` en los 5 tests existentes |

---

## Tarea 5 — Verificación y Pruebas de Humo en AWS Real ❌ NO INICIADA (0%)

| Ítem | Estado |
|------|--------|
| Perfil AWS real (`application-aws.yml`) | ✅ Creado — `application-aws.yml` sin credenciales ni endpoint |
| Pruebas automatizadas de integración contra AWS | ❌ No existen |
| Smoke tests end-to-end | ❌ No implementados |
| Configuración de colas reales | ⚠️ Pendiente de crear en AWS |
| Documento de procedimiento manual | ✅ Existe (`M2Communication/m2-event-simulation-manual-test.md`) |

**Riesgo:** La aplicación ya cuenta con el perfil `aws`. Para migrar a AWS real se requiere:
1. ✅ Perfil `application-aws.yml` creado (sin `endpoint`, usa `DefaultCredentialsProvider`)
2. ⚠️ Crear las colas reales en AWS con los nombres: `solicitudes-ruta-queue`, `logistics-eventos-paquete`, `respuestas-ruta-queue`, `paquete-listo-clasificar-queue`
3. ✅ Nombres de colas fijos (sin prefijo dinámico) — compatibles con entorno real

---

## Tarea 6 — Contextualización sobre Módulo Profesor ❌ NO INICIADA (0%)

- Directorio `PendingWork/TutorModule/` **vacío**
- No hay archivos, referencias ni documentación relacionada con el Módulo Profesor en el proyecto

---

## Tarea 7 — JSON para Módulo Profesor ❌ NO INICIADA (0%)

- No se encontraron archivos JSON, schemas, DTOs ni endpoints relacionados con el Módulo Profesor

---

## Hallazgos Arquitectónicos Adicionales

| # | Hallazgo | Severidad | Archivo | Estado |
|---|----------|-----------|---------|--------|
| H1 | `Paquete.java` usa `@Setter` a nivel de clase | 🔴 ALTA | `domain/model/Paquete.java:14` | ✅ **Resuelto en PR8** — `@Setter` eliminado. |
| H2 | `Persona.java` usa anotaciones `jakarta.validation` en dominio | ⚠️ MEDIA | `domain/model/Persona.java` — viola pureza del dominio | ✅ **Resuelto** — `Persona.java` no tiene anotaciones `jakarta.validation`. `@Embeddable` eliminado en PR8. |
| H3 | `PaqueteDbo.java` aplana `Direccion` a String en vez de columnas separadas | ⚠️ MEDIA | `infrastructure/adapter/persistence/paquete/PaqueteDbo.java` | ❌ Pendiente |
| H4 | `PaqueteMapper.java` intenta instanciar `Persona` con constructor sin ser `@AllArgsConstructor` | ⚠️ MEDIA | `infrastructure/adapter/persistence/paquete/PaqueteMapper.java` | ❌ Pendiente |
| H5 | `GeocodingService` — firma inconsistente: interfaz `localizar(String)`, uso con `Direccion`, adapter con `Direccion` | ⚠️ MEDIA | `application/ports/GeocodingService.java` vs `GoogleMapsAdapter.java` | ❌ Pendiente |
| H6 | ~~`RutaEventAdapter` deprecated pero aún presente en el código~~ | ~~✅ BAJA~~ | ❌ **Resuelto en PR3** — `RutaEventAdapter` y `RutaEventPublisher` eliminados | ✅ Resuelto |
| H7 | Sin `UNIQUE CONSTRAINT` en tabla `eventos_procesados` para idempotencia | ⚠️ MEDIA | Riesgo de condición de carrera (documentado en `sqs-m2-contrat-discrepancies-fix.md`) | ❌ Pendiente |
| H8 | Perfil `aws` no definido en M1 para listeners SQS | ⚠️ MEDIA | M2 sí usa `@Profile("aws")`, M1 no. Los listeners de M1 siempre activos. | ✅ **Resuelto en PR4** — `@Profile({"default", "local", "aws"})` añadido a los 3 listeners |
| H9 | Comunicación M1→M3 migrada de REST síncrono a SQS asíncrono | ✅ COMPLETADA | Se eliminó endpoint síncrono `ConsultaFinancieraController` y se creó `FinanzasEventSqsAdapter` publicando a `eventos-financieros-paquete-queue` | ✅ **Resuelto en PR9** |

---

## Resumen Numérico de Hallazgos

| Severidad | Cantidad | Descripción |
|-----------|:--------:|-------------|
| 🔴 CRÍTICO | 0 | ~~`@Setter` en dominio~~ — **Resuelto en PR8**. Fecha outbound + inbound + enums resueltos en PR6/PR7. |
| ⚠️ ALTA | 1 | RUTA_ASIGNADA no implementado en M2 |
| ⚠️ MEDIA | 3 | `volumen_m3` extra, unique constraint, `Direccion` aplanada |
| ✅ Resuelto | 15 | H1 (PR8), H2 (PR8), H6 (PR3), H8 (PR4), Dirección (compatible), fecha_limite_entrega (PR6), Enums TipoMercancia/MetodoPago (PR6), **fecha_hora_evento inbound (PR7)**, **MotivoParadaFallida/TipoNovedadGrave (PR7)**, **mapper fechaHoraEvento (PR7)**, **Observación 4 registrarNovedad (PR8)**, **Obs. 5 M3 sync→async (PR9)**, **Obs. 6 Publisher integrado (PR9)**, **Obs. 8 Tests M3 (PR9)** |
| ✅ Sin novedad | ~30+ | Campos UUID, nombres de eventos, estructura de SQS, polimorfismo, notificaciones, historial, endpoints |

> [!IMPORTANT]
> Con la eliminación del `RutaEventAdapter` en PR3, la discrepancia del prefijo DEV (`carlos-`) en la cola `solicitudes-ruta-event-queue` ya no aplica, pues dicha cola ya no es referenciada por ningún componente. El hallazgo H6 y su referencia cruzada en Tarea 1 están resueltos.

---

## Recomendaciones Prioritarias

### Inmediatas (Bloqueantes)
1. ~~🔴 **Cambiar `OffsetDateTime` → `Instant`** en los 6 DTOs de eventos M2 (inbound) y convertir en `EventoPaqueteM2Mapper`~~ — **Resuelto en PR7**
2. ~~🔴 **Eliminar `@Setter` de `Paquete.java`** e implementar métodos de negocio explícitos~~ — **Resuelto en PR8**

### Corto plazo
4. ⚠️ Exigir a M2 la implementación del producer para `respuestas-ruta-queue` (`RUTA_ASIGNADA`)
5. ⚠️ Agregar `UNIQUE CONSTRAINT` en `eventos_procesados(evento_id)` para idempotencia robusta
6. ~~⚠️ Eliminar `jakarta.validation.*` del dominio (`Persona.java`)~~ — **Resuelto — `Persona.java` no contenía dichas anotaciones; `@Embeddable` eliminado en PR8**
7. ⚠️ Separar `Direccion` en columnas individuales en `PaqueteDbo.java`
8. ~~🔴 Eliminar endpoint REST síncrono de M3 (`ConsultaFinancieraController`)~~ — **Resuelto en PR9**

### Mediano plazo
9. ✅ Perfil `application-aws.yml` creado. Pruebas de humo en AWS pendientes.
10. ✅ Migración M3 a SQS asíncrono completada. Validar que M3 consuma correctamente `eventos-financieros-paquete-queue`.
11. Iniciar análisis del Módulo Profesor (Tareas 6-7)
12. Agregar integración continua con pruebas contra LocalStack en CI

### ✅ Resueltas en PR6
- 🔴 `fecha_limite_entrega`: `OffsetDateTime` → `Instant` en outbound M1→M2
- ⚠️ Enums `TipoMercancia` y `MetodoPago` → `String` en payload de salida

### ✅ Resueltas en PR7 (`bugfix/m2-inbound-contracts`)
- 🔴 `fecha_hora_evento` inbound: `OffsetDateTime` → `Instant` en 7 DTOs de eventos M2
- ⚠️ `MotivoParadaFallida` y `TipoNovedadGrave`: de enum a `String` + try-catch con log.warn y fallback
- ⚠️ `EventoPaqueteM2Mapper`: ahora asigna `fechaHoraEvento` a `EventoRutaDto` (antes solo se usaba para ID)

### ✅ Resueltas en PR8 (`bugfix/domain-immutability-and-states`)
- 🔴 `@Setter` eliminado de `Paquete.java` (class-level y field `etiquetaDigital`). Creadas fábricas `crearNuevo()` y `reconstruir()`.
- ⚠️ `@Embeddable` eliminado de `Persona.java`.
- ⚠️ `Paquete.reconstruir()` público para hidratación desde infraestructura; `@NoArgsConstructor` privado.
- ⚠️ `RegistrarAdmisionUseCase`: UUID generado por el caso de uso (no por infraestructura).
- ⚠️ `PriceCalculationService`: recibe `distanciaKm` como parámetro explícito en vez de leer de `Paquete`.
- ⚠️ `registrarNovedad()`: ahora recibe `NovedadBodega` VO preservando `tipoNovedad` en `HistorialEstado`. Flyway V7: columna `tipo_novedad`.
- ⚠️ Tests refactorizados sin `new Paquete()`, `setEstado()` ni `setTipoMercancia()`.

### ✅ Resueltas en PR9 (`feature/m3-async-sqs-migration`)
- 🔴 **Migración M3 (Finanzas) de REST síncrono a SQS asíncrono.**
- ❌ Eliminados componentes síncronos: `ConsultaFinancieraController`, `ConsultarEstadoPaqueteUseCase`, `GestionNovedadPaqueteResponse`.
- ✅ Creado puerto `EstadoPaqueteFinanzasPublisher` (interfaz hexagonal).
- ✅ Creado DTO `EventoFinancieroPaqueteDto` con `@JsonProperty` snake_case.
- ✅ Creado adaptador `FinanzasEventSqsAdapter` usando `SqsTemplate`.
- ✅ `springdoc.paths-to-match` cambiado a `/api/**` (eliminado `/route/**`).
- ✅ Publisher integrado en `ProcesarEventoRutaUseCase` (eventos M2).
- ✅ Publisher integrado en `RegistrarNovedadUseCase` (novedades M1).
- ✅ `FinanzasEventSqsAdapterTest` con 6 escenarios de publicación.
- ✅ `ProcesarEventoRutaUseCaseTest` actualizado con verificación M3.
- ✅ `RegistrarNovedadUseCaseTest` actualizado con mock + aserciones `verify`/`never` para Finanzas (5 tests).

### ✅ Resueltas en Bloque 2 (garantía transaccional)
- 🔴 **FinanzasEventSqsAdapter**: catch → throw para propagar excepción SQS y permitir rollback de transacción `@Transactional`.
- ✅ `FinanzasEventSqsAdapterTest`: nuevo test `testPropagarExcepcionCuandoSqsFalla` que verifica que la excepción se propaga.
- 🔴 **RegistrarNovedadUseCaseTest**: añadido `@Mock EstadoPaqueteFinanzasPublisher` + aserciones `verify`/`never` en los 5 tests existentes para evitar NPE.

---

*Documento generado automáticamente por el Agente de Revisión Arquitectónica — 2026-05-23*
*Actualizado post Bloque 2 (feature/m3-async-sqs-migration)*
*Fuente: `Task.md`, código fuente en `src/`, documentación en `Backend/Externo/`*

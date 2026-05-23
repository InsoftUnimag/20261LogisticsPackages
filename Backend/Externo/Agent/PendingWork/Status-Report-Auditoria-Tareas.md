# Reporte de Estado del Proyecto — Auditoría vs Task.md

**Fecha:** 2026-05-23  
**Última actualización:** 2026-05-23 (post PR6: bugfix/m2-outbound-contracts)  
**Auditor:** Agente de Revisión Arquitectónica  
**Documento fuente:** `Backend/Externo/Agent/PendingWork/Task.md`

---

## Resumen Ejecutivo

| Tarea | Estado | Progreso Estimado |
|-------|--------|:------------------:|
| **1.** Migración RabbitMQ → Amazon SQS | ✅ **Completada** | 100% |
| **2.** Definición/Diseño flujo M2 (Contratos e Infraestructura) | ✅ **Completada** | 100% |
| **3.** Implementación secuencial de las 3 colas de M2 | ⚠️ **Completada con riesgos** | ~95% |
| **4.** Actualización UC/IP 007 (Gestión Novedades — Síncrono a Asíncrono) | ⚠️ **Completada con observaciones** | ~85% |
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

#### 🔴 CRÍTICO — `fecha_hora_evento`: OffsetDateTime vs Instant

| Lado | Tipo | Afecta |
|------|------|--------|
| **M1** (actual) | `OffsetDateTime` + `@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")` | 6 DTOs de eventos M2 |
| **M2** (real) | `Instant` | `PaqueteEnTransitoEvento`, `PaqueteEntregadoEvento`, `ParadaFallidaEvento`, `NovedadGraveEvento`, `ParadasSinGestionarEvento`, `PaqueteExcluidoDespachoEvento` |
| **Archivo base** | `infrastructure/dto/event/EventoPaqueteM2Dto.java:44-45` | Clase abstracta padre |

**Recomendación:** Cambiar el tipo en los DTOs de infraestructura a `Instant` y convertir a `OffsetDateTime` en `EventoPaqueteM2Mapper`.

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
| `MotivoParadaFallida` (enum M1) vs `String` (M2) | Posible error de deserialización | `ParadaFallidaEvento.java` |
| `TipoNovedadGrave` (enum M1) vs `String` (M2) | Posible error de deserialización | `NovedadGraveEvento.java` |

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
| Mapper | ✅ `EventoPaqueteM2Mapper` — cubre los 6 tipos de eventos |
| ⚠️ Riesgo | `OffsetDateTime` vs `Instant` puede causar `DateTimeParseException` en runtime |

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

## Tarea 4 — Actualización UC/IP 007 (Síncrono → Asíncrono) ⚠️ COMPLETADA CON OBSERVACIONES (85%)

### Componentes implementados — ✅

| Componente | Archivo | Funcionalidad |
|------------|---------|---------------|
| `ProcesarEventoRutaUseCase` | `application/usecase/gestionnovedad/ProcesarEventoRutaUseCase.java` | Orquestador: idempotencia → actualizar estado → historial → notificaciones |
| `ConsultarEstadoPaqueteUseCase` | `application/usecase/gestionnovedad/ConsultarEstadoPaqueteUseCase.java` | Consulta síncrona para M3 Finanzas |
| `ConsultaFinancieraController` | `infrastructure/controller/ConsultaFinancieraController.java` | `GET /route/{idRoute}/package/{idPaquete}` |
| `EventoRutaDto` | `application/usecase/gestionnovedad/EventoRutaDto.java` | DTO con 6 `TipoEventoRuta` |
| `RutaEventSqsListener` | `infrastructure/adapter/messaging/RutaEventSqsListener.java` | Consumo asíncrono de eventos M2 |
| `EventoPaqueteM2Mapper` | `infrastructure/adapter/messaging/EventoPaqueteM2Mapper.java` | Mapeo M2 DTOs → `EventoRutaDto` |
| Idempotencia (FR-008) | `domain/model/EventoProcesado.java` + Flyway V5 | Tabla `eventos_procesados` con validación |
| Notificaciones (FR-002) | `application/ports/NotificacionPort.java` + `MockNotificacionAdapter` | SMS + Email con manejo de fallos |
| Endpoint síncrono (FR-005) | `ConsultaFinancieraController` | `GET /route/{idRoute}/package/{idPaquete}` |
| 6 transiciones de estado en `Paquete.java` | `domain/model/Paquete.java` | `transitarAEnRuta()`, `entregarPaquete()`, `registrarDevolucionEnRuta()`, etc. |
| DTO de respuesta financiera | `application/usecase/gestionnovedad/GestionNovedadPaqueteResponse.java` (antes `ConsultaPaqueteResponse`) | Renombrado en PR2 para eliminar ambigüedad |

### Observaciones — ⚠️

| # | Hallazgo | Severidad | Detalle | Estado |
|---|----------|-----------|---------|--------|
| 1 | **`@Setter` a nivel de clase en `Paquete.java`** | 🔴 **ALTA** | Viola el principio de inmutabilidad del dominio. Cualquier código puede mutar el estado sin pasar por métodos de negocio. Documentado en `ConsideracionesAgentes.md` | ❌ Pendiente |
| 2 | **`EN_PARADA_DE_ENTREGA` sin evento M2 correspondiente** | ⚠️ MEDIA | El `ProcesarEventoRutaUseCase` lo maneja, pero M2 no tiene evento equivalente. El mapper nunca generará este tipo. | ❌ Pendiente |
| 3 | **Mapper no recibe `fechaHoraEvento`** | ⚠️ MEDIA | En `EventoPaqueteM2Mapper.construirDto()`, `fechaHoraEvento` no se usa para construir el `EventoRutaDto` (línea 74-90), solo para generar el `eventoId` | ❌ Pendiente |
| 4 | **`registrarNovedad()` usa `NOVEDAD_EN_BODEGA` fijo** | ⚠️ MEDIA | `Paquete.registrarNovedad()` cambia siempre a `NOVEDAD_EN_BODEGA` sin distinguir subtipos de novedad | ❌ Pendiente |

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
| H1 | `Paquete.java` usa `@Setter` a nivel de clase | 🔴 ALTA | `domain/model/Paquete.java:14` | ❌ Pendiente |
| H2 | `Persona.java` usa anotaciones `jakarta.validation` en dominio | ⚠️ MEDIA | `domain/model/Persona.java` — viola pureza del dominio | ❌ Pendiente |
| H3 | `PaqueteDbo.java` aplana `Direccion` a String en vez de columnas separadas | ⚠️ MEDIA | `infrastructure/adapter/persistence/paquete/PaqueteDbo.java` | ❌ Pendiente |
| H4 | `PaqueteMapper.java` intenta instanciar `Persona` con constructor sin ser `@AllArgsConstructor` | ⚠️ MEDIA | `infrastructure/adapter/persistence/paquete/PaqueteMapper.java` | ❌ Pendiente |
| H5 | `GeocodingService` — firma inconsistente: interfaz `localizar(String)`, uso con `Direccion`, adapter con `Direccion` | ⚠️ MEDIA | `application/ports/GeocodingService.java` vs `GoogleMapsAdapter.java` | ❌ Pendiente |
| H6 | ~~`RutaEventAdapter` deprecated pero aún presente en el código~~ | ~~✅ BAJA~~ | ❌ **Resuelto en PR3** — `RutaEventAdapter` y `RutaEventPublisher` eliminados | ✅ Resuelto |
| H7 | Sin `UNIQUE CONSTRAINT` en tabla `eventos_procesados` para idempotencia | ⚠️ MEDIA | Riesgo de condición de carrera (documentado en `sqs-m2-contrat-discrepancies-fix.md`) | ❌ Pendiente |
| H8 | Perfil `aws` no definido en M1 para listeners SQS | ⚠️ MEDIA | M2 sí usa `@Profile("aws")`, M1 no. Los listeners de M1 siempre activos. | ✅ **Resuelto en PR4** — `@Profile({"default", "local", "aws"})` añadido a los 3 listeners |

---

## Resumen Numérico de Hallazgos

| Severidad | Cantidad | Descripción |
|-----------|:--------:|-------------|
| 🔴 CRÍTICO | 1 | `@Setter` en dominio (fecha outbound resuelta en PR6) |
| ⚠️ ALTA | 2 | RUTA_ASIGNADA no implementado en M2, `jakarta.validation` en dominio |
| ⚠️ MEDIA | 5 | `volumen_m3` extra, mapper incompleto, unique constraint, transición fija, `Direccion` aplanada |
| ✅ Resuelto | 5 | H6 (PR3), H8 (PR4), Dirección (M2 acepta objeto), fecha_limite_entrega (PR6), Enums a String (PR6) |
| ✅ Sin novedad | ~30+ | Campos UUID, nombres de eventos, estructura de SQS, polimorfismo, notificaciones, historial, endpoints |

> [!IMPORTANT]
> Con la eliminación del `RutaEventAdapter` en PR3, la discrepancia del prefijo DEV (`carlos-`) en la cola `solicitudes-ruta-event-queue` ya no aplica, pues dicha cola ya no es referenciada por ningún componente. El hallazgo H6 y su referencia cruzada en Tarea 1 están resueltos.

---

## Recomendaciones Prioritarias

### Inmediatas (Bloqueantes)
1. 🔴 **Cambiar `OffsetDateTime` → `Instant`** en los 6 DTOs de eventos M2 (inbound) y convertir en `EventoPaqueteM2Mapper`
2. 🔴 **Eliminar `@Setter` de `Paquete.java`** e implementar métodos de negocio explícitos

### Corto plazo
4. ⚠️ Exigir a M2 la implementación del producer para `respuestas-ruta-queue` (`RUTA_ASIGNADA`)
5. ⚠️ Agregar `UNIQUE CONSTRAINT` en `eventos_procesados(evento_id)` para idempotencia robusta
6. ⚠️ Eliminar `jakarta.validation.*` del dominio (`Persona.java`)
7. ⚠️ Separar `Direccion` en columnas individuales en `PaqueteDbo.java`

### Mediano plazo
8. ✅ Perfil `application-aws.yml` creado. Pruebas de humo en AWS pendientes.
9. Iniciar análisis del Módulo Profesor (Tareas 6-7)
10. Agregar integración continua con pruebas contra LocalStack en CI

### ✅ Resueltas en PR6
- 🔴 `fecha_limite_entrega`: `OffsetDateTime` → `Instant` en outbound M1→M2
- ⚠️ Enums `TipoMercancia` y `MetodoPago` → `String` en payload de salida

---

*Documento generado automáticamente por el Agente de Revisión Arquitectónica — 2026-05-23*
*Actualizado post PR6 (bugfix/m2-outbound-contracts)*
*Fuente: `Task.md`, código fuente en `src/`, documentación en `Backend/Externo/`*

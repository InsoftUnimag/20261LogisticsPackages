# Auditoría de Código Cruzada: Módulo 1 (Packages) ↔ Módulo 2 (Routes)

**Fecha de auditoría:** 2026-05-23
**Auditor:** Agente de Integración — Arquitectura de Mensajería SQS
**Versión del documento:** 1.1 (actualizado con M3)

---

## 1. Estructura y Flujo de la Comunicación

### 1.1 Colas Compartidas

| Cola | Dirección | M1 (Producer) | M1 (Consumer) | M2 (Producer) | M2 (Consumer) | M3 (Consumer) |
|------|-----------|--------------|---------------|---------------|---------------|---------------|
| `solicitudes-ruta-queue` | M1 → M2 | `RutaSqsAdapter` | `SolicitarRutaConsumer` | — | — | — |
| `logistics-eventos-paquete` | M2 → M1 | `SqsIntegracionModulo1Adapter` | `RutaEventSqsListener` | — | — | — |
| `respuestas-ruta-queue` | M2 → M1 | `RutaSqsListener` | — | **NO IMPLEMENTADO** | — | — |
| `eventos-financieros-paquete-queue` | M1 → M3 | `FinanzasEventSqsAdapter` | — | — | — | M3 (Finanzas) |

### 1.2 Mapa de Interacciones Detallado

```
┌─────────────────────────────────────────────────────────────────────────┐
│  MÓDULO 1 — LogisticsPackages (SGP)                                     │
│                                                                         │
│  [RutaSqsAdapter]  ──send──>  solicitudes-ruta-queue                    │
│       │                      (SOLICITAR_RUTA)                           │
│       │                                                                 │
│  [FinanzasEventSqsAdapter]  ──send──>  eventos-financieros-paquete-queue│
│       │                       (ESTADO_FINAL_PAQUETE — hacia Módulo 3)   │
│       │                                                                 │
│  [RutaEventSqsListener]   ◄──receive──  logistics-eventos-paquete       │
│       │                                    (6 tipos de eventos M2)      │
│       │                                                                 │
│  [RutaSqsListener]   ◄──receive──  respuestas-ruta-queue                │
│       │                       (RUTA_ASIGNADA — SIN IMPLEMENTAR EN M2)   │
└─────────────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  MÓDULO 2 — LogisticsRoutes                                             │
│                                                                         │
│  [SolicitarRutaConsumer]  ◄──receive──  solicitudes-ruta-queue          │
│       │                      (SOLICITAR_RUTA)                           │
│       │                                                                 │
│  [SqsIntegracionModulo1Adapter]  ──send──>  logistics-eventos-paquete    │
│       │                       (PAQUETE_EN_TRANSITO, PAQUETE_ENTREGADO,  │
│       │                        PARADA_FALLIDA, NOVEDAD_GRAVE,           │
│       │                        PARADAS_SIN_GESTIONAR,                   │
│       │                        PAQUETE_EXCLUIDO_DESPACHO)               │
│       │                                                                 │
│  [SqsIntegracionModulo3Adapter]  ──send──>  cierre-ruta-queue           │
│       │                       (RUTA_CERRADA — hacia Módulo 3)           │
└─────────────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  MÓDULO 3 — Finanzas                                                    │
│                                                                         │
│  [M3 Consumer]  ◄──receive──  eventos-financieros-paquete-queue          │
│       │                       (ESTADO_FINAL_PAQUETE — desde M1)         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.3 Clases de Infraestructura Involucradas

**Módulo 1 (Producer / Consumer):**

| Clase | Rol | Cola |
|-------|-----|------|
| `com.logistics.packages.infrastructure.adapter.messaging.RutaSqsAdapter` | Producer | `solicitudes-ruta-queue` |
| `com.logistics.packages.infrastructure.adapter.messaging.RutaEventSqsListener` | Consumer | `logistics-eventos-paquete` |
| `com.logistics.packages.infrastructure.adapter.messaging.RutaSqsListener` | Consumer | `respuestas-ruta-queue` |
| `com.logistics.packages.infrastructure.adapter.messaging.FinanzasEventSqsAdapter` | Producer (M1→M3) | `eventos-financieros-paquete-queue` |

**Módulo 2 (Consumer / Producer):**

| Clase | Rol | Cola |
|-------|-----|------|
| `com.logistics.routes.infrastructure.adapter.in.messaging.SolicitarRutaConsumer` | Consumer | `solicitudes-ruta-queue` |
| `com.logistics.routes.infrastructure.adapter.out.messaging.SqsIntegracionModulo1Adapter` | Producer | `logistics-eventos-paquete` |

---

## 2. Detalles de Contratos de Datos (M1 vs M2)

### 2.1 SOLICITAR_RUTA — M1 (Producer) → M2 (Consumer)

**M1:** `com.logistics.packages.infrastructure.dto.request.SolicitudRutaPayload`
**M2:** `com.logistics.routes.infrastructure.dto.request.SolicitarRutaRequest`

| Campo JSON | M1 — Tipo en código | M2 — Tipo en código | Estado |
|------------|---------------------|---------------------|--------|
| `tipo_evento` | `String` (hardcodeado "SOLICITAR_RUTA") | No existe en el record (no se usa internamente) | OK |
| `paquete_id` | `UUID` | `UUID` | OK |
| `peso_kg` | `Double` | `Double` | OK |
| `volumen_m3` | `Double` | **NO EXISTE** | ⚠️ CAMPO FALTANTE |
| `direccion` | `DireccionDto` (objeto anidado: `direccion`, `ciudad`, `pais`) | Objeto (DTO anidado) | ✅ Compatible — M2 acepta objeto dirección |
| `latitud` | `Double` | `Double` | OK |
| `longitud` | `Double` | `Double` | OK |
| `fecha_limite_entrega` | `OffsetDateTime` con formato `yyyy-MM-dd'T'HH:mm:ssXXX` | `Instant` | ⚠️ TIPO INCOMPATIBLE |
| `tipo_mercancia` | `TipoMercancia` (enum de M1) | `String` | ⚠️ ENUM vs STRING |
| `metodo_pago` | `MetodoPago` (enum de M1) | `String` | ⚠️ ENUM vs STRING |

### 2.2 PAQUETE_EN_TRANSITO — M2 (Producer) → M1 (Consumer)

**M2:** `com.logistics.routes.application.event.PaqueteEnTransitoEvent`
**M1:** `com.logistics.packages.infrastructure.dto.event.PaqueteEnTransitoEvento`

| Campo JSON | M2 — Tipo en código | M1 — Tipo en código | Estado |
|------------|---------------------|---------------------|--------|
| `tipo_evento` | `String` ("PAQUETE_EN_TRANSITO") | `String` | OK |
| `paquete_id` | `UUID` | `UUID` | OK |
| `ruta_id` | `UUID` | `UUID` | OK |
| `fecha_hora_evento` | `Instant` | `Instant` (sin `@JsonFormat`) | ✅ RESUELTO (vía M1 — PR7) |
| `evidencia` | No existe | No existe | OK |

### 2.3 PAQUETE_ENTREGADO — M2 (Producer) → M1 (Consumer)

**M2:** `com.logistics.routes.application.event.PaqueteEntregadoEvent`
**M1:** `com.logistics.packages.infrastructure.dto.event.PaqueteEntregadoEvento`

| Campo JSON | M2 — Tipo en código | M1 — Tipo en código | Estado |
|------------|---------------------|---------------------|--------|
| `tipo_evento` | `String` ("PAQUETE_ENTREGADO") | `String` | OK |
| `paquete_id` | `UUID` | `UUID` | OK |
| `ruta_id` | `UUID` | `UUID` | OK |
| `fecha_hora_evento` | `Instant` | `Instant` (sin `@JsonFormat`) | ✅ RESUELTO (vía M1 — PR7) |
| `evidencia` | Record anidado `Evidencia` (`urlFoto`, `urlFirma`) | `EvidenciaDto` anidado (`urlFoto`, `urlFirma`) | OK |

### 2.4 PARADA_FALLIDA — M2 (Producer) → M1 (Consumer)

**M2:** `com.logistics.routes.application.event.ParadaFallidaEvent`
**M1:** `com.logistics.packages.infrastructure.dto.event.ParadaFallidaEvento`

| Campo JSON | M2 — Tipo en código | M1 — Tipo en código | Estado |
|------------|---------------------|---------------------|--------|
| `tipo_evento` | `String` ("PARADA_FALLIDA") | `String` | OK |
| `paquete_id` | `UUID` | `UUID` | OK |
| `ruta_id` | `UUID` | `UUID` | OK |
| `fecha_hora_evento` | `Instant` | `Instant` (sin `@JsonFormat`) | ✅ RESUELTO (vía M1 — PR7) |
| `motivo` | `String` (valor plano del enum de M2) | `String` + fallback `mapMotivo()` | ✅ RESUELTO (vía M1 — PR7) |

### 2.5 NOVEDAD_GRAVE — M2 (Producer) → M1 (Consumer)

**M2:** `com.logistics.routes.application.event.NovedadGraveEvent`
**M1:** `com.logistics.packages.infrastructure.dto.event.NovedadGraveEvento`

| Campo JSON | M2 — Tipo en código | M1 — Tipo en código | Estado |
|------------|---------------------|---------------------|--------|
| `tipo_evento` | `String` ("NOVEDAD_GRAVE") | `String` | OK |
| `paquete_id` | `UUID` | `UUID` | OK |
| `ruta_id` | `UUID` | `UUID` | OK |
| `fecha_hora_evento` | `Instant` | `Instant` (sin `@JsonFormat`) | ✅ RESUELTO (vía M1 — PR7) |
| `tipo_novedad` | `String` (valor plano del enum de M2) | `String` + fallback `mapTipoNovedad()` | ✅ RESUELTO (vía M1 — PR7) |

### 2.6 PARADAS_SIN_GESTIONAR — M2 (Producer) → M1 (Consumer)

**M2:** `com.logistics.routes.application.event.ParadasSinGestionarEvent`
**M1:** `com.logistics.packages.infrastructure.dto.event.ParadasSinGestionarEvento`

| Campo JSON | M2 — Tipo en código | M1 — Tipo en código | Estado |
|------------|---------------------|---------------------|--------|
| `tipo_evento` | `String` ("PARADAS_SIN_GESTIONAR") | `String` | OK |
| `ruta_id` | `UUID` | `UUID` (heredado de abstracta) | OK |
| `tipo_cierre` | `String` (`tipoCierre.name()`) | `String` | OK |
| `fecha_hora_evento` | `Instant` | `Instant` (sin `@JsonFormat`) | ✅ RESUELTO (vía M1 — PR7) |
| `paquetes` | `List<PaqueteRef(UUID paqueteId)>` | `List<PaqueteEnRutaDto(UUID paqueteId)>` | OK |

### 2.7 PAQUETE_EXCLUIDO_DESPACHO — M2 (Producer) → M1 (Consumer)

**M2:** `com.logistics.routes.application.event.PaqueteExcluidoDespachoEvent`
**M1:** `com.logistics.packages.infrastructure.dto.event.PaqueteExcluidoDespachoEvento`

| Campo JSON | M2 — Tipo en código | M1 — Tipo en código | Estado |
|------------|---------------------|---------------------|--------|
| `tipo_evento` | `String` ("PAQUETE_EXCLUIDO_DESPACHO") | `String` | OK |
| `paquete_id` | `UUID` | `UUID` | OK |
| `ruta_id` | `UUID` | `UUID` | OK |
| `fecha_hora_evento` | `Instant` | `Instant` (sin `@JsonFormat`) | ✅ RESUELTO (vía M1 — PR7) |

---

## 3. Validación de Configuraciones

### 3.1 Declaración de Colas en application.yml

| Cola | M1 — Property | M1 — Valor por defecto | M2 — Property | M2 — Valor por defecto |
|------|---------------|------------------------|---------------|------------------------|
| Solicitud de ruta | `aws.sqs.ruta-request-queue` | `solicitudes-ruta-queue` | `app.sqs.solicitudes-ruta-queue` | `solicitudes-ruta-queue` |
| Eventos de paquete | `app.sqs.eventos-paquete-queue` | `logistics-eventos-paquete` | `app.sqs.eventos-paquete-queue` | `logistics-eventos-paquete` |
| Respuesta de ruta | `aws.sqs.ruta-response-queue` | `respuestas-ruta-queue` | — | — |
| Eventos financieros paquete | `app.sqs.eventos-financieros-queue` | `eventos-financieros-paquete-queue` | — | — |

### 3.2 Análisis de Desalineaciones

| # | Hallazgo | Severidad | Ubicación |
|---|----------|-----------|------------|
| 1 | ~~**Prefijo DEV (`carlos-`):** M1 usa `${DEV_PREFIX:carlos}` en producción local~~ | ~~⚠️ MEDIA~~ | **✅ Resuelto en PR4** — `${DEV_PREFIX}` eliminado, colas con nombres fijos |
| 2 | ~~**Perfil AWS:** M1 no tiene perfil `aws` definido para los listeners SQS~~ | ~~⚠️ MEDIA~~ | **✅ Resuelto en PR4** — `@Profile({"default", "local", "aws"})` añadido a los 3 listeners |
| 3 | **Cola `respuestas-ruta-queue` huérfana:** M1 escucha en `respuestas-ruta-queue` pero M2 no tiene ningún producer hacia esa cola. El flujo `RUTA_ASIGNADA` está incompleto. | 🔴 ALTA | M1: `RutaSqsListener:20`, M2: N/A |
| 4 | **Cola `cierre-ruta-queue` fuera del alcance:** M2 produce hacia `cierre-ruta-queue` (hacia M3), pero M1 no tiene ningún listener para esta cola. Esto es correcto ya que M3 debería consumirlas. | N/A | M2: `SqsIntegracionModulo3Adapter` |
| 5 | **Nueva cola M1→M3:** M1 ahora publica a `eventos-financieros-paquete-queue` para comunicación asíncrona con M3 (Finanzas). Se eliminó el endpoint REST síncrono `GET /route/{idRoute}/package/{idPaquete}`. | ✅ COMPLETADA | M1: `FinanzasEventSqsAdapter` → `eventos-financieros-paquete-queue` |

### 3.3 Configuración de Deserialización Jackson

| Configuración | M1 | M2 |
|---------------|----|----|
| Snake case strategy | `@JsonProperty` explícitos con `snake_case` | `@JsonNaming(SnakeCaseStrategy.class)` a nivel de clase |
| Polimorfismo en eventos M2→M1 | `@JsonTypeInfo` + `@JsonSubTypes` en `EventoPaqueteM2Dto` | `@JsonNaming` en cada record de evento |
| `fail-on-unknown-properties` | No especificado (default: false) | `false` en M2 `application.yml` |
| `write-dates-as-timestamps` | No especificado | `false` en M2 `application.yml` |

---

## 4. Dictamen de Integración

### 4.1 Resumen de Hallazgos

| Severidad | Cantidad | Descripción |
|-----------|----------|-------------|
| 🔴 CRÍTICO | 0 | ~~Incompatibilidad de tipo de fecha (OffsetDateTime vs Instant)~~ **✅ Resuelto en PR7** |
| ⚠️ ALTA | 1 | Flujo RUTA_ASIGNADA no implementado en M2 |
| ⚠️ MEDIA | 0 | ~~Enums M1 vs strings M2~~ **✅ Resuelto en PR7** |
| ✅ Resuelto | 6 | Prefijo DEV (PR4), perfil AWS (PR4), dirección (compatible), **fecha_hora_evento (PR7)**, **MotivoParadaFallida (PR7)**, **TipoNovedadGrave (PR7)** |
| ✅ OK | ~30 | Campos UUID, nombres de eventos, estructura de evidencia, nombres de colas |

### 4.2 Veredicto: LA COMUNICACIÓN NO ES 100% COMPATIBLE

La integración entre M1 y M2 presenta incompatibilidades que **causarán excepciones en tiempo de ejecución** si no se refactorizan. A continuación las refactorizaciones recomendadas:

> [!NOTE]
> La discrepancia de `direccion` (objeto vs String) está **resuelta**: M2 acepta `direccion` como objeto estructurado, no requiere cambio.

### 4.3 Refactorizaciones Inmediatas Recomendadas (Orden de Prioridad)

#### ✅ PRIORIDAD 1 — Incompatibilidad de Tipo de Fecha (`Instant` vs `OffsetDateTime`) — RESUELTA

**Problema:** Los 6 eventos que M2 envía tienen `fecha_hora_evento` como `java.time.Instant`. M1 esperaba `OffsetDateTime` con formato `"yyyy-MM-dd'T'HH:mm:ssXXX"`.

**Solución aplicada (vía M1 — PR7):** Se cambió M1 para aceptar `Instant` directamente en los 7 DTOs de eventos, eliminando `@JsonFormat`. El mapper `EventoPaqueteM2Mapper` convierte `Instant` → `OffsetDateTime` con `ZoneOffset.UTC` al construir `EventoRutaDto`.

**Archivos modificados en M1:**
- `infrastructure/dto/event/EventoPaqueteM2Dto.java` — tipo `Instant`, sin `@JsonFormat`
- `infrastructure/dto/event/PaqueteEnTransitoEvento.java` — constructor `Instant`
- `infrastructure/dto/event/PaqueteEntregadoEvento.java` — constructor `Instant`
- `infrastructure/dto/event/PaqueteExcluidoDespachoEvento.java` — constructor `Instant`
- `infrastructure/dto/event/ParadaFallidaEvento.java` — constructor `Instant`
- `infrastructure/dto/event/NovedadGraveEvento.java` — constructor `Instant`
- `infrastructure/dto/event/ParadasSinGestionarEvento.java` — constructor `Instant`
- `infrastructure/adapter/messaging/EventoPaqueteM2Mapper.java` — conversión `Instant.atOffset(ZoneOffset.UTC)`

**Rama:** `bugfix/m2-inbound-contracts`

#### ✅ RESUELTA — DTO de Dirección

**Problema:** ~~M1 deserializa `direccion` como `DireccionDto` con campos `direccion`, `ciudad`, `pais`. M2 envía `direccion` como un `String` plano.~~

**Estado:** ✅ **Compatible.** M2 acepta `direccion` como objeto estructurado (`DireccionDto`). No se requiere ningún cambio ni en M1 ni en M2.

#### ⚠️ PRIORIDAD 3 — Flujo RUTA_ASIGNADA no implementado en M2

**Problema:** M1 tiene un listener en `respuestas-ruta-queue` esperando `RUTA_ASIGNADA` de M2, pero M2 no tiene ningún producer hacia esa cola.

**Solución:** Implementar en M2 un adapter similar a `SqsIntegracionModulo1Adapter` que envíe un evento `RutaAsignadaEvent` con los campos `tipo_evento`, `paquete_id`, `ruta_id`, `fecha_hora_evento`.

#### ✅ PRIORIDAD 4 — Enum vs String en `motivo` y `tipo_novedad` — RESUELTA

**Problema:** M1 definía `ParadaFallidaEvento.MotivoParadaFallida` y `NovedadGraveEvento.TipoNovedadGrave` como enums, pero M2 los envía como strings planos.

**Solución aplicada (vía M1 — PR7):**
- Campos cambiados a `String` en los DTOs para deserialización segura.
- En el mapper, métodos `mapMotivo()` y `mapTipoNovedad()` con `try-catch`:
  - `try` limitado a `Enum.valueOf()` (solo la línea que falla).
  - `catch` → `log.warn()` + valor por defecto (`MOTIVO_DESCONOCIDO` o `DEVOLUCION`).

**Archivos modificados en M1:**
- `infrastructure/dto/event/ParadaFallidaEvento.java` — `motivo` como `String`
- `infrastructure/dto/event/NovedadGraveEvento.java` — `tipoNovedad` como `String`
- `infrastructure/adapter/messaging/EventoPaqueteM2Mapper.java` — `mapMotivo()` + `mapTipoNovedad()` con fallback

**Rama:** `bugfix/m2-inbound-contracts`

#### ⚠️ PRIORIDAD 5 — Campo `volumen_m3` faltante en M2

**Problema:** M1 tiene el campo `volumen_m3` (Double) que M2 no envía.

**Recomendación:** Agregar el campo `volumenM3` al `SolicitarRutaRequest` de M2, alimentándolo desde el `Paquete.getVolumenM3()`.

### 4.4 Conformidades que No Requieren Cambio

| Componente | Estado | Razón |
|------------|--------|-------|
| Arquitectura hexagonal | ✅ | Los adapters SQS están aislados en `infrastructure.adapter` en ambos módulos |
| Naming strategy | ✅ | M2 usa `@JsonNaming(SnakeCaseStrategy)` que genera snake_case consistente |
| Tipos UUID | ✅ | Todos los campos `paquete_id`, `ruta_id` son `UUID` en ambos lados |
| Nombres de eventos | ✅ | `PAQUETE_EN_TRANSITO`, `PAQUETE_ENTREGADO`, etc. coinciden exactamente |
| Evidencia POD | ✅ | `url_foto` y `url_firma` coinciden en estructura |
| Polimorfismo JSON | ✅ | `@JsonTypeInfo` + `@JsonSubTypes` alineados con los `tipo_evento` de M2 |
| Nombres de colas base | ✅ | `solicitudes-ruta-queue`, `logistics-eventos-paquete` y `respuestas-ruta-queue` alineados con M2 |

---

## 5. Matriz de Impacto por Flujo

| Flujo | Tipo Evento | Compatibilidad | Acción Requerida |
|-------|------------|-----------------|-----------------|
| M1 → M2 | SOLICITAR_RUTA | ⚠️ PARCIAL | Cambiar dirección a DTO, agregar volumen_m3, convertir Instant → OffsetDateTime |
| M2 → M1 | PAQUETE_EN_TRANSITO | ✅ COMPATIBLE | Resuelto vía M1 (PR7) — DTOs aceptan Instant |
| M2 → M1 | PAQUETE_ENTREGADO | ✅ COMPATIBLE | Resuelto vía M1 (PR7) — DTOs aceptan Instant |
| M2 → M1 | PARADA_FALLIDA | ✅ COMPATIBLE | Resuelto vía M1 (PR7) — Instant + fallback motivo |
| M2 → M1 | NOVEDAD_GRAVE | ✅ COMPATIBLE | Resuelto vía M1 (PR7) — Instant + fallback tipoNovedad |
| M2 → M1 | PARADAS_SIN_GESTIONAR | ✅ COMPATIBLE | Resuelto vía M1 (PR7) — DTOs aceptan Instant |
| M2 → M1 | PAQUETE_EXCLUIDO_DESPACHO | ✅ COMPATIBLE | Resuelto vía M1 (PR7) — DTOs aceptan Instant |
| M2 → M1 | RUTA_ASIGNADA | 🔴 NO IMPLEMENTADO | Implementar producer en M2 hacia `respuestas-ruta-queue` |
| M1 → M3 | ESTADO_FINAL_PAQUETE | ✅ COMPLETADO | Migrado de REST síncrono a SQS asíncrono vía `FinanzasEventSqsAdapter` → `eventos-financieros-paquete-queue` |

---

*Documento generado automáticamente por el Agente de Auditoría de Integración SQS.*

# Plan de Documentación — Mensajería SQS (Módulo 1 Logística)

## Estado: COMPLETADO ✅

> Este documento refleja el estado final del plan tras la ejecución de las correcciones.

---

## Fuente de Verdad (ARNs Reales)

### Producidas por M1 (Cuenta: 214654654786):
- `arn:aws:sqs:us-east-2:214654654786:eventos-financieros-paquete-queue` → consumido por M3 (183678668012)
- `arn:aws:sqs:us-east-2:214654654786:solicitudes-ruta-queue` → consumido por M2 (383941187903)

### Consumidas por M1 (Cuenta: 383941187903):
- `arn:aws:sqs:us-east-2:383941187903:logistics-eventos-paquete`
- `arn:aws:sqs:us-east-2:383941187903:respuestas-ruta-queue`

> **Regla**: Solo existen estas 4 colas. No se utilizan prefijos como `${DEV_PREFIX:carlos}`.

---

## Las únicas 4 colas reales

| # | Cola | ARN | Dueño | Rol M1 |
|---|------|-----|-------|--------|
| 1 | `solicitudes-ruta-queue` | `arn:aws:sqs:us-east-2:214654654786:solicitudes-ruta-queue` | **Cuenta 214654654786 (M1)** | **Produce** (M1→M2) |
| 2 | `eventos-financieros-paquete-queue` | `arn:aws:sqs:us-east-2:214654654786:eventos-financieros-paquete-queue` | **Cuenta 214654654786 (M1)** | **Produce** (M1→M3 183678668012) |
| 3 | `logistics-eventos-paquete` | `arn:aws:sqs:us-east-2:383941187903:logistics-eventos-paquete` | Cuenta 383941187903 (M2) | **Consume** (M2→M1) |
| 4 | `respuestas-ruta-queue` | `arn:aws:sqs:us-east-2:383941187903:respuestas-ruta-queue` | Cuenta 383941187903 (M2) | **Consume** (M2→M1) |

---

## Correcciones Ejecutadas

### Archivos ELIMINADOS (5)

| Archivo | Ruta | Motivo |
|---------|------|--------|
| `ClasificacionEventAdapter.java` | `infrastructure/adapter/messaging/` | Cola fantasma `paquete-listo-clasificar-queue` |
| `PaqueteListoClasificacionSqsListener.java` | `infrastructure/adapter/messaging/` | Cola fantasma `paquete-listo-clasificar-queue` |
| `ClasificacionEventPublisher.java` | `application/ports/` | Puerto huérfano |
| `NovedadEventAdapter.java` | `infrastructure/adapter/messaging/` | Cola fantasma `novedad-registrada-queue` |
| `NovedadEventPublisher.java` | `application/repository/` | Puerto huérfano |

### Archivos MODIFICADOS (5)

| Archivo | Cambio |
|---------|--------|
| `application.properties` | Eliminado `${DEV_PREFIX:carlos}`, corregido `solicitar-ruta-queue` → `solicitudes-ruta-queue`, corregido `eventos-paquete-queue` → `logistics-eventos-paquete`, eliminada línea `clasificacion-queue` |
| `application.yml` | Eliminada línea `clasificacion-queue: paquete-listo-clasificar-queue` |
| `PrepararAlmacenajeUseCase.java` | Reemplazado `ClasificacionEventPublisher` + llamada SQS por inyección directa de `ClasificarPaqueteUseCase` + invocación directa a `sugerirZonaParaPaquete()` |
| `RegistrarNovedadUseCase.java` | Eliminada dependencia `NovedadEventPublisher` y llamada `publicarNovedadRegistrada()` |
| `RegistrarNovedadUseCaseTest.java` | Eliminado mock de `NovedadEventPublisher` y 5 verificaciones asociadas |

---

## Documentos Generados

| Documento | Descripción |
|-----------|-------------|
| `Consideraciones.md` | Aspectos arquitectónicos y de flujo SQS entre M1, M2, M3 |
| `IntegracionEnFrontend.md` | Estrategias async para frontend (SSE, WebSocket, Polling) |
| `CredencialesLocales.md` | Mapeo técnico de cada cola SQS real con payloads y permisos |

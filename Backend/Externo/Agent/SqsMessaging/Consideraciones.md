# Consideraciones Arquitectónicas — Mensajería SQS

## Arquitectura General de Colas

```
Cuenta AWS 214654654786 (M1 - Logística)
  ├── solicitudes-ruta-queue                 → Producido por M1, consumido por M2 (383941187903)
  └── eventos-financieros-paquete-queue      → Producido por M1, consumido por M3 (183678668012)

Cuenta AWS 383941187903 (M2 - Rutas)
  ├── logistics-eventos-paquete              → Producido por M2, consumido por M1
  └── respuestas-ruta-queue                  → Producido por M2, consumido por M1
```

### Características Transversales
- **Cross-Account SQS**: M1 produce a su propia cuenta (214654654786) pero consume de cuentas ajenas (M2: 383941187903). M3 (183678668012) consume desde la cola de M1 vía política de recursos cross-account.
- **Consistencia Eventual**: No hay garantía de orden ni de tiempo de procesamiento.
- **Sin DLQ en colas de terceros**: M1 no puede configurar Dead-Letter Queues en las colas propiedad de M2.

---

## Vista Módulo 1 (Logística)

### Flujo de Publicación

| Cola | Adaptador | Puerto | Frecuencia | Error Handling |
|------|-----------|--------|------------|----------------|
| `solicitudes-ruta-queue` | `RutaSqsAdapter` | `RutaQueuePort` | Por cada paquete admitido | Lanza `SqsCommunicationException` → rollback transacción |
| `eventos-financieros-paquete-queue` | `FinanzasEventSqsAdapter` | `EstadoPaqueteFinanzasPublisher` | Solo en estados finales (ENTREGADO, DAÑADO, EXTRAVIADO, DEVOLUCIÓN, NOVEDAD_EN_BODEGA) | Re-lanza excepción |

### Flujo de Consumo

| Cola | Listener | Puerto/UseCase | Estrategia de Errores |
|------|----------|----------------|----------------------|
| `logistics-eventos-paquete` | `RutaEventSqsListener` | `ProcesarEventoRutaUseCase` | Idempotencia vía `EventoProcesadoRepository`. Eventos duplicados se loguean y descartan. Errores de negocio relanzan excepción (SQS reintenta). |
| `respuestas-ruta-queue` | `RutaSqsListener` | `AsignarRutaUseCase` | Validación de `tipoEvento == RUTA_ASIGNADA` y `rutaId != null`. Mensajes inválidos se descartan con warn. |

### Idempotencia
- `ProcesarEventoRutaUseCase` verifica `EventoProcesadoRepository.yaFueProcesado(eventoId)` antes de procesar.
- `eventoId` se genera con formato `M2:{TIPO}:{paqueteId}:{timestamp}`.
- Si el evento ya fue procesado, lanza `EventoDuplicadoException` que el listener captura gracefulmente.

### Recomendaciones para Consumo desde Cuenta Ajena
- No relanzar excepción para mensajes corruptos o no deseados (evita reintentos infinitos en cola M2).
- Loguear y retornar (mensaje se eliminará de la cola tras `visibilityTimeout`).
- Para errores transitorios (DB caída), SQS reintentará automáticamente.

---

## Vista Módulo 2 (Rutas)

### Responsabilidades SQS
- **Consume** `solicitudes-ruta-queue` (producida por M1).
- **Produce** `logistics-eventos-paquete` y `respuestas-ruta-queue` (consumidas por M1).

### Contrato de Idempotencia y Consistencia
- M2 debe garantizar que cada solicitud de ruta se procesa exactamente una vez.
- M1 confía en que M2 responderá eventualmente vía `respuestas-ruta-queue` (RUTA_ASIGNADA) o `logistics-eventos-paquete` (eventos de tracking).
- M2 debe configurar políticas de recursos en sus colas para permitir `sqs:ReceiveMessage` y `sqs:DeleteMessage` a la cuenta M1.

---

## Vista Módulo 3 (Finanzas)

### Responsabilidades SQS
- **Consume** `eventos-financieros-paquete-queue` (producida por M1, cuenta 214654654786). M3 accede vía política de recursos cross-account.
- M3 debe implementar idempotencia al procesar eventos (M1 puede reintentar envíos).

### Estados que Gatillan Liquidación
M1 publica a M3 solo cuando el paquete alcanza un estado final:

| Estado | Descripción |
|--------|-------------|
| `ENTREGADO` | Ciclo completado exitosamente → liquidar pago al transportador |
| `DAÑADO_EN_RUTA` | Paquete dañado → ajuste de liquidación o penalización |
| `EXTRAVIADO_EN_RUTA` | Pérdida → compensación al cliente |
| `DEVOLUCION_EN_RUTA` | No entregado por motivo operativo → liquidación parcial |
| `NOVEDAD_EN_BODEGA` | Novedad en bodega (dañado/extraviado) → liquidación interna |

### Consistencia Eventual
- M1 publica después de persistir el estado final en DB (garantía de al menos una entrega).
- M3 debe ser idempotente al recibir múltiples mensajes del mismo paquete.
- El payload `EventoFinancieroPaqueteDto` contiene `id_paquete`, `id_ruta`, `estado`.

---

## Matriz de Latencia y Consistencia

| Cola | Latencia Esperada | Estrategia ante Fallo | DLQ |
|------|-------------------|-----------------------|-----|
| `solicitudes-ruta-queue` | Segundos a minutos | Reintentos SQS automáticos | Configurable en cuenta M1 |
| `eventos-financieros-paquete-queue` | Minutos a horas | Reintentos SQS automáticos | Configurable en cuenta M1 |
| `logistics-eventos-paquete` | Segundos a minutos | Log + descarte (M1 no puede configurar DLQ en M2) | No disponible (cuenta M2) |
| `respuestas-ruta-queue` | Minutos a horas | Log + descarte (M1 no puede configurar DLQ en M2) | No disponible (cuenta M2) |

---

## Recomendaciones de Migración (Ejecutadas)

| Tarea | Estado |
|-------|--------|
| Eliminar `ClasificacionEventAdapter` | ✅ Completado |
| Eliminar `PaqueteListoClasificacionSqsListener` | ✅ Completado |
| Eliminar `ClasificacionEventPublisher` | ✅ Completado |
| Eliminar `NovedadEventAdapter` | ✅ Completado |
| Eliminar `NovedadEventPublisher` | ✅ Completado |
| Corregir `application.properties`: eliminar DEV_PREFIX | ✅ Completado |
| Corregir `application.properties`: `solicitar-ruta-queue` → `solicitudes-ruta-queue` | ✅ Completado |
| Corregir `application.properties`: `eventos-paquete-queue` → `logistics-eventos-paquete` | ✅ Completado |
| Eliminar `clasificacion-queue` de `application.yml` | ✅ Completado |
| Refactorizar `PrepararAlmacenajeUseCase` (invocación directa) | ✅ Completado |
| Refactorizar `RegistrarNovedadUseCase` (eliminar publisher fantasma) | ✅ Completado |
| Actualizar tests | ✅ Completado |

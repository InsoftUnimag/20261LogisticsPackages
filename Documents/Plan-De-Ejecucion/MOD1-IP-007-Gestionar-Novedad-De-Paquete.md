# Implementation Plan: Gestionar Novedad de Paquete (MOD1-UC-007)

**Date**: 2026-04-19
**Spec**: [Gestionar Novedad de Paquete](../Specs/MOD1-UC-007-Gestionar-Novedad-De-Paquete.md)

---

## Summary

Como Controlador de Novedades, necesito recibir y procesar eventos del ciclo de vida del paquete (tanto de bodega como del Módulo de Rutas) para mantener la trazabilidad y exponer un estado consolidado al Módulo de Finanzas. Esta funcionalidad actuará como un centro neurálgico, consumiendo eventos asíncronos (AMQP/SQS) para actualizaciones de estado y exponiendo un endpoint REST síncrono (`GET /route/{idRoute}/package/{idPaquete}`) para consultas financieras, garantizando la integridad de los datos y la comunicación entre módulos.

---

## Technical Context

| Campo | Valor |
|---|---|
| **Language/Version** | Java 21 (Backend) / JavaScript (React para Frontend) |
| **Primary Dependencies** | Spring Web, Spring Data JPA, Spring Security, Spring Validation, PostgreSQL, Spring Boot Starter AMQP / AWS SQS, Gradle |
| **Storage** | PostgreSQL (para el paquete y su historial de estados inmutable) |
| **Testing** | JUnit 5, Mockito, Testcontainers (PostgreSQL, RabbitMQ/Localstack) |
| **Target Platform** | Servidor Linux para Backend API y Web browser para la UI |
| **Project Type** | Extensión de Single Web Application (Backend / Web) |
| **Performance Goals** | Endpoint de consulta financiera < 500ms (p95). Procesamiento de eventos asíncronos < 10s. |
| **Constraints** | Endpoint de consulta debe ser síncrono. Procesamiento de eventos debe ser idempotente. |
| **Scale/Scope** | Módulo central para la trazabilidad de extremo a extremo y la integración financiera. |
| **Framework** | Spring Boot 3.x (Backend), React (Frontend) |
| **Arquitectura** | Hexagonal (Ports & Adapters) — Event-Driven y API síncrona |
| **Mensajería** | Escucha eventos de `novedad_bodega` y `evento_ruta` (del Módulo 2). Publica notificaciones a usuarios. |

---

## Project Structure

> Se extiende la arquitectura hexagonal para manejar la recepción de eventos de múltiples fuentes y exponer un nuevo endpoint de consulta síncrono.

```text
frontend/
└── src/
    ├── components/
    │   └── novedad/
    │       └── VisorNovedades.jsx      [NUEVO — UI para ver el historial de un paquete]
    └── services/
        └── PaqueteApiService.js        [MODIFICADO — Añadir método para consultar historial]

backend/
├── domain/
│   ├── model/
│   │   ├── Paquete.java                [MODIFICADO — Métodos para manejar nuevos estados de ruta]
│   │   └── Notificacion.java           [NUEVO — Entidad para gestionar notificaciones a usuarios]
│   ├── exception/
│   │   └── EventoDuplicadoException.java [NUEVO]
│   └── external/
│           ├── NotificacionPort.java     [NUEVO — Port para enviar SMS/Email]
│           └── EventoProcesadoRepository.java [NUEVO — Para garantizar idempotencia]
│
├── application/
│   └── gestionnovedad/
│       ├── ProcesarEventoRutaUseCase.java [NUEVO — Orquestador para eventos del Módulo 2]
│       ├── ConsultarEstadoPaqueteUseCase.java [NUEVO — Orquestador para el endpoint síncrono]
│
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   ├── web/
    │   │   │   └── ConsultaFinancieraController.java [NUEVO — Endpoint síncrono para Finanzas]
    │   │   └── messaging/
    │   │       └── RutaEventListener.java    [NUEVO — Listener para eventos del Módulo 2]
    │   └── out/
    │       ├── notification/
    │       │   └── SnsEmailNotificationAdapter.java [NUEVO — Implementación para notificar]
    │       └── persistence/
    │           └── EventoProcesadoJpaRepository.java [NUEVO — Tabla para idempotencia]
    └── dto/
        └── response/
            └── ConsultaPaqueteResponse.java [NUEVO — Payload para el Módulo de Finanzas]
```

---

## Phase 1: Prerequisitos (verificación)

- [ ] T701 Verificar que `build.gradle` tenga las dependencias de Spring AMQP/SQS y, opcionalmente, SDKs para notificación (SNS, Twilio).
- [ ] T702 Validar que la migración de Flyway para la tabla `eventos_procesados` se ejecute correctamente.
- [ ] T703 Confirmar la configuración de las colas de entrada para eventos de ruta.

---

## Phase 2: Dominio — Manejo de Estados de Ruta e Idempotencia

**Purpose**: Adaptar el dominio para manejar los nuevos estados provenientes del Módulo de Rutas y asegurar que los eventos no se procesen dos veces.

### Tests del dominio (TDD)

- [ ] T704 [P] Test unitario `PaqueteRutaTest`:
  - `transitarAEnRuta()`: cambia el estado a `EN_TRANSITO` y genera un `HistorialEstado`.
  - `transitarAEntregado()`: cambia el estado a `ENTREGADO`, asocia la evidencia (POD) y genera un `HistorialEstado`.
  - No se puede transitar a un estado anterior (ej. de `ENTREGADO` a `EN_TRANSITO`).
- [ ] T705 [P] Test unitario para el servicio de dominio de idempotencia:
  - `marcarComoProcesado()`: guarda un ID de evento.
  - `yaFueProcesado()`: devuelve `true` si el ID de evento ya existe.

### Implementación del dominio

- [ ] T706 [P] Modificar `Paquete.java` para añadir los métodos que manejen las transiciones de estado provenientes del Módulo 2 (`enTransito`, `enParadaDeEntrega`, `entregado`, `devolucionEnRuta`, etc.).
- [ ] T707 [P] Crear la entidad `EventoProcesado` para almacenar los IDs de los eventos ya procesados y garantizar la idempotencia (FR-008).

---

## Phase 3: Servicios de Aplicación — Orquestación de Eventos y Consultas (US7)

**Goal**: Implementar los casos de uso que manejan los flujos de datos entrantes (asíncronos) y salientes (síncronos).

### Tests del servicio (TDD)

- [ ] T708 [P] [US7] `ProcesarEventoRutaUseCaseTest` — Evento de "Entregado":
  - Dado: Un evento válido de `paquete_entregado` del Módulo 2.
  - Cuando: Se ejecuta `procesar()`.
  - Entonces: Se actualiza el paquete, se guarda el historial, se marca el evento como procesado y se dispara una notificación al remitente/destinatario.
- [ ] T709 [P] [US7] `ProcesarEventoRutaUseCaseTest` — Evento Duplicado:
  - Dado: Un ID de evento que ya ha sido procesado.
  - Cuando: Se ejecuta `procesar()`.
  - Entonces: Se lanza `EventoDuplicadoException` y no se realiza ninguna acción.
- [ ] T710 [P] [US7] `ConsultarEstadoPaqueteUseCaseTest` — Consulta exitosa:
  - Dado: Un `paqueteId` y `rutaId` válidos.
  - Cuando: Se ejecuta `consultar()`.
  - Entonces: Retorna un `ConsultaPaqueteResponse` con los datos del paquete.

### Implementación del servicio

- [ ] T711 [P] [US7] Implementar `ProcesarEventoRutaUseCase` para manejar los eventos del Módulo 2.
- [ ] T712 [P] [US7] Implementar `ConsultarEstadoPaqueteUseCase` para la consulta síncrona de Finanzas.

---

## Phase 4: Adaptadores de Entrada y Salida

### Adaptadores de Entrada (Backend)

- [ ] T713 [P] [US7] Implementar `RutaEventListener` que escuche la cola de eventos del Módulo 2 y llame a `ProcesarEventoRutaUseCase`.
- [ ] T714 [P] [US7] Crear `ConsultaFinancieraController` con el endpoint `GET /route/{idRoute}/package/{idPaquete}` que llame a `ConsultarEstadoPaqueteUseCase`.
- [ ] T715 [US7] Implementar un `ControllerAdvice` para que el endpoint de consulta devuelva 404 si el paquete no se encuentra y 500 si hay un error interno (FR-007, SC-005).

### Adaptadores de Salida (Backend)

- [ ] T716 [US7] Implementar `SnsEmailNotificationAdapter` (o similar) que implemente `NotificacionPort` para enviar notificaciones a los usuarios (FR-002).
- [ ] T717 [US7] Implementar `EventoProcesadoJpaAdapter` para persistir los IDs de los eventos procesados.

### Aplicación UI (Frontend)

- [ ] T718 [US7] Desarrollar el componente `VisorNovedades.jsx` que muestre el historial de estados de un paquete, obtenido desde el backend.

---

## Dependencies & Execution Order

- **Dependencia**: Este plan depende de los eventos generados por `MOD1-UC-006` (novedades en bodega) y por el Módulo 2 (eventos en ruta).
- **Orden**:
    1.  **Phase 2 (Dominio)**: Definir las nuevas transiciones de estado y la lógica de idempotencia.
    2.  **Phase 3 (Servicios)**: Implementar los casos de uso para procesar eventos y para consultas.
    3.  **Phase 4 (Adaptadores)**: Crear los listeners de mensajería, el controller REST y los adaptadores de notificación.

## Notes

- La idempotencia es clave (FR-008). La tabla `eventos_procesados` es una forma robusta de lograrlo, almacenando el ID único de cada mensaje consumido.
- El contrato del payload del evento del Módulo 2 debe estar bien definido y ser estable.
- El endpoint para Finanzas es síncrono y de solo lectura, lo que simplifica su implementación y lo aísla de la complejidad de los flujos asíncronos.

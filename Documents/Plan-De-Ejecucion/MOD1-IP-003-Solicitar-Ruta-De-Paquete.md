# Implementation Plan: Solicitar Ruta de Paquete (MOD1-IP-003)

**Date**: 2026-04-15
**Spec**: [Solicitar Ruta de Paquete](../Specs/MOD1-UC-003-Solicitar-Ruta-De-Paquete.md)

## Summary

Como Empleado de Envío y Recepción, necesito que el sistema envíe automáticamente una solicitud de ruta al `Módulo de Gestión de Rutas` después de que un paquete haya sido admitido y pesado con éxito. Esto se logrará a través de una comunicación estrictamente asíncrona utilizando JSON sobre una cola de mensajes (Amazon SQS). El sistema construirá un payload con los detalles del paquete, lo enviará y procesará la respuesta para almacenar un `ID de ruta`, manejando reintentos en caso de timeouts.

## Technical Context

| Campo | Valor |
|---|---|
| **Language/Version** | Java 21 (Backend) |
| **Primary Dependencies** | Spring Cloud AWS SQS, Spring Web, Spring Data JPA |
| **Storage** | PostgreSQL (actualización del `Paquete` con `rutaId`) |
| **Testing** | JUnit 5, Mockito, Testcontainers (LocalStack) |
| **Target Platform** | Servidor Linux (Backend) |
| **Project Type** | Extensión de Single Web Application (Backend) |
| **Performance Goals** | Envío de solicitud de ruta < 500ms tras evento. |
| **Constraints** | Comunicación estrictamente asíncrona. La solicitud de ruta se emite una sola vez. |
| **Scale/Scope** | Componente crítico para la planificación logística. |
| **Framework** | Spring Boot 3.x |
| **Arquitectura** | Hexagonal (Ports & Adapters) — Event-Driven |
| **Mensajería** | Cola asíncrona (Event-Driven) para `solicitar_ruta` y recibir `respuesta_ruta`. |

## Project Structure

> Se extiende la arquitectura hexagonal para incluir adaptadores de mensajería que gestionan la comunicación con el Módulo de Gestión de Rutas.

```text
backend/
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── logistics
    │   │           └── packages
    │   │               ├── application
    │   │               │   ├── ports
    │   │               │   │   └── RutaQueuePort.java           [NUEVO - Interfaz para enviar/recibir]
    │   │               │   ├── usecase
    │   │               │   │   ├── AsignarRutaUseCase.java      [NUEVO - Procesa la respuesta]
    │   │               │   │   └── SolicitarRutaUseCase.java    [NUEVO - Envía la solicitud]
    │   │               ├── domain
    │   │               │   ├── model
    │   │               │   │   └── Paquete.java                 [MODIFICADO - Añadir rutaId y estado]
    │   │               │   └── event
    │   │               │       └── SolicitudRutaEvent.java      [NUEVO - Evento de dominio]
    │   │               └── infrastructure
    │   │                   ├── adapter
    │   │                   │   └── messaging
    │   │                   │       ├── RutaSqsAdapter.java     [NUEVO - Implementación SQS]
    │   │                   │       ├── RutaSqsListener.java [NUEVO - Listener SQS para respuestas]
    │   │                   ├── dto
    │   │                   │   ├── request
    │   │                   │   │   └── SolicitudRutaPayload.java  [NUEVO - Payload JSON]
    │   │                   │   └── response
    │   │                   │       └── RespuestaRutaPayload.java  [NUEVO - Payload JSON]
    │   │                   └── persistence
    │   │                       └── paquete
    │   │                           └── PaqueteJpaAdapter.java   [MODIFICADO - método de actualización]
    └── test
        └── java
            └── com
                └── logistics
                    └── packages
                        ├── application
                        │   └── usecase
                        │       ├── AsignarRutaUseCaseTest.java   [NUEVO]
                        │       └── SolicitarRutaUseCaseTest.java [NUEVO]
```

---

## Phase 1: Dominio y Eventos

**Purpose**: Actualizar el modelo `Paquete` para reflejar la asignación de ruta y definir los eventos de dominio asociados.

### Tests del dominio (TDD)

- [ ] T301 [P] Test unitario `PaqueteRutaTest`:
  - `asignarRuta()`: verificar que el `rutaId` se asigna correctamente y el estado del paquete cambia a `EN_TRANSITO` o similar.
  - No se puede asignar una ruta si ya tiene una (`rutaId` no es nulo).

### Implementación del dominio

- [ ] T302 [P] Modificar `Paquete.java` para incluir el `rutaId`:
```java
// backend/domain/model/Paquete.java
public class Paquete {
    // ... atributos existentes ...

    // Asignación de Rutas (MOD1-UC-003)
    private UUID rutaId;

    public void asignarRuta(UUID rutaId) {
        if (this.rutaId != null) {
            throw new IllegalStateException("El paquete ya tiene una ruta asignada.");
        }
        this.rutaId = rutaId;
        this.estado = EstadoPaquete.LISTO_PARA_ENVIO; // O el estado que corresponda
    }
}
```
- [ ] T303 [P] Crear el evento de dominio `SolicitudRutaEvent` para desacoplar la lógica de negocio del publicador de eventos.

---

## Phase 2: Casos de Uso de Solicitud y Asignación

**Purpose**: Implementar los casos de uso para enviar la solicitud de ruta y para procesar la respuesta asíncrona.

### Tests de los casos de uso (TDD)

- [x] T304 [P] [US3] `SolicitarRutaUseCaseTest`:
  - Dado: Un `Paquete` completo (admitido y pesado).
  - Cuando: Se ejecuta `solicitarRuta()`.
  - Entonces: Se pasa el `Paquete` al puerto `RutaQueuePort.enviarSolicitud(Paquete)` (el mapeo a DTO ocurre en el adaptador).
- [ ] T305 [P] [US3] `AsignarRutaUseCaseTest`:
  - Dado: Un `RespuestaRutaPayload` con un `paqueteId` y `rutaId`.
  - Cuando: Se ejecuta `asignarRuta()`.
  - Entonces: Se busca el `Paquete`, se le asigna la ruta y se guarda con `paqueteRepository.actualizar()`.

### Implementación de los casos de uso

- [x] T306 [P] [US3] Implementar `SolicitarRutaUseCase` que es invocado por un evento tras el pesaje:
```java
// backend/application/usecase/SolicitarRutaUseCase.java
@Service
public class SolicitarRutaUseCase {

    private final PaqueteRepository paqueteRepository;
    private final RutaQueuePort rutaQueuePort;

    public void handle(SolicitudRutaEvent event) {
        Paquete paquete = paqueteRepository.findById(event.getPaqueteId())
            .orElseThrow(() -> new PaqueteNotFoundException(event.getPaqueteId()));

        // FR-001 + FR-002: Pasa el dominio al puerto; el adaptador mapea a DTO
        rutaQueuePort.enviarSolicitud(paquete);
    }
}
```
- [ ] T307 [P] [US3] Implementar `AsignarRutaUseCase` para procesar la respuesta:
```java
// backend/application/usecase/AsignarRutaUseCase.java
@Service
@Transactional
public class AsignarRutaUseCase {

    private final PaqueteRepository paqueteRepository;

    public AsignarRutaUseCase(PaqueteRepository paqueteRepository) {
        this.paqueteRepository = paqueteRepository;
    }

    public void asignarRuta(RespuestaRutaPayload payload) {
        Paquete paquete = paqueteRepository.findById(payload.getPaqueteId())
            .orElseThrow(() -> new PaqueteNotFoundException(payload.getPaqueteId()));
        
        // FR-003: Almacenar el ID de ruta
        paquete.asignarRuta(payload.getRutaId());
        
        paqueteRepository.actualizar(paquete);
    }
}
```

---

## Phase 3: Adaptadores de Mensajería (Infrastructure)

**Purpose**: Implementar la comunicación real con el sistema de colas (Amazon SQS).

### Tests de los adaptadores

- [ ] T308 [US3] Test de integración con `Testcontainers` (LocalStack) para `RutaSqsAdapter`:
  - Verificar que un mensaje se envía correctamente a una cola SQS.
- [ ] T309 [US3] Test de integración para `RutaSqsListener`:
  - Simular la recepción de un mensaje en la cola SQS de respuesta y verificar que `AsignarRutaUseCase` es invocado.

### Implementación de los adaptadores

- [x] T310 [P] [US3] Implementar `RutaSqsAdapter` que implemente `RutaQueuePort`:
```java
// backend/infrastructure/adapter/messaging/RutaSqsAdapter.java
@Component
public class RutaSqsAdapter implements RutaQueuePort {

    private final SqsTemplate sqsTemplate;

    @Value("${aws.sqs.ruta-request-queue:${DEV_PREFIX:carlos}-solicitar-ruta-queue}")
    private String queueName;

    @Override
    public void enviarSolicitud(Paquete paquete) {
        // El mapeo de dominio a DTO ocurre dentro del adaptador (hexagonal)
        SolicitudRutaPayload payload = mapToPayload(paquete);
        try {
            sqsTemplate.send(to -> to.queue(queueName).payload(payload));
        } catch (Exception e) {
            log.error("Error enviando solicitud de ruta", e);
        }
    }

    private SolicitudRutaPayload mapToPayload(Paquete paquete) {
        return SolicitudRutaPayload.builder()
            .tipoEvento("SOLICITAR_RUTA")
            .paqueteId(paquete.getId())
            .pesoKg(paquete.getPeso().getKilogramos())
            .volumenM3(paquete.getVolumenM3())
            .direccion(new DireccionDto(paquete.getDireccionDestino().getDireccion(),
                                         paquete.getDireccionDestino().getCiudad(),
                                         paquete.getDireccionDestino().getPais()))
            .latitud(paquete.getCoordenadas().latitud())
            .longitud(paquete.getCoordenadas().longitud())
            .fechaLimiteEntrega(paquete.getFechaIngresoUtc().plusDays(7)
                                .atOffset(ZoneOffset.UTC).toString())
            .tipoMercancia(paquete.getTipoMercancia().name())
            .metodoPago(paquete.getMetodoPago().name())
            .build();
    }
}
```
- [ ] T311 [P] [US3] Implementar `RutaSqsListener` para la cola de respuestas:
```java
// backend/infrastructure/adapter/messaging/RutaSqsListener.java
@Component
public class RutaSqsListener {

    private final AsignarRutaUseCase asignarRutaUseCase;

    @SqsListener("${aws.sqs.ruta-response-queue:respuestas-ruta-queue}")
    public void recibirRespuesta(RespuestaRutaPayload payload) {
        log.info("Mensaje recibido de la cola de respuestas de ruta: {} - Estado: {}",
                payload.getPaqueteId(), payload.getEstado());
        asignarRutaUseCase.asignarRuta(payload);
    }
}
```
- [x] T312 [US3] Configurar las colas SQS y sus propiedades (URL, región) en `application.properties`, `application.yml` y `application-local.yml`. El nombre de la cola de solicitudes usa prefijo dinámico: `${DEV_PREFIX:carlos}-solicitar-ruta-queue`.
- [ ] T313 [US3] Implementar la lógica de reintentos (FR-005) en caso de timeout, posiblemente usando una Dead Letter Queue (DLQ) en Amazon SQS.

---

## Dependencies & Execution Order

- **Dependencia**: Este plan se ejecuta automáticamente después de que `MOD1-IP-001` y `MOD1-IP-002` se completen para un paquete. Un evento de dominio (`PaquetePesadoEvent` o similar) debe disparar este flujo.
- **Orden**:
    1.  **Phase 1 (Dominio)**: Modificar el `Paquete`.
    2.  **Phase 2 (Casos de Uso)**: Crear los orquestadores de negocio.
    3.  **Phase 3 (Adaptadores)**: Implementar la comunicación externa asíncrona.

## Notes

- La generación del evento que inicia este flujo (por ejemplo, `PaquetePesadoEvent`) debe ser implementada al final del `ProcesarPesajeUseCase` del plan `MOD1-IP-002`.
- La gestión de timeouts y reintentos (FR-005) es crucial y debe ser robusta. El uso de una Dead Letter Queue (DLQ) en Amazon SQS es la práctica recomendada para manejar fallos persistentes en la comunicación.
- Se debe definir un contrato claro (schema JSON) para los payloads de solicitud y respuesta para asegurar la compatibilidad con el `Módulo de Gestión de Rutas`.
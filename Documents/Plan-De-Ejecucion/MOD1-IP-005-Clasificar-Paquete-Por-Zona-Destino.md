# Implementation Plan: Clasificar Paquete por Zona de Destino (MOD1-IP-005)

**Date**: 2026-04-17
**Spec**: [Clasificar Paquete por Zona de Destino](../Specs/MOD1-UC-005-Clasificar-Paquete-Por-Zona-Destino.md)

## Summary

Como Almacenista, necesito que el sistema asigne a cada paquete su zona de destino lógica, basada en la proximidad geográfica del destinatario. Este proceso es invocado asíncronamente después de que el paquete es ubicado físicamente en la bodega (UC-004). El sistema calculará y sugerirá la zona, y tras la confirmación del almacenista, el estado del paquete se actualizará a `Listo para Despacho`, preparando el terreno para la consolidación de carga por parte del Módulo de Rutas.

## Technical Context

| Campo | Valor |
|---|---|
| **Language/Version** | Java 21 (Backend) / JavaScript (React para Frontend) |
| **Primary Dependencies** | Spring Cloud AWS SQS, Spring Web, Spring Data JPA, Spring Validation |
| **Storage** | PostgreSQL (actualización del `Paquete` con `zonaDestinoId`) |
| **Testing** | JUnit 5, Mockito, Testcontainers (LocalStack/PostgreSQL) |
| **Target Platform** | Servidor Linux para Backend API y Web browser para la UI |
| **Project Type** | Extensión de Single Web Application (Backend / Web) |
| **Performance Goals** | Respuesta de cálculo de zona < 2s. |
| **Constraints** | La clasificación solo puede ocurrir para paquetes en estado `EN_CLASIFICACION`. |
| **Scale/Scope** | Flujo clave para la optimización de la logística de despacho. |
| **Framework** | Spring Boot 3.x (Backend), React (Frontend) |
| **Arquitectura** | Hexagonal (Ports & Adapters) — Event-Driven |
| **Mensajería** | Escucha un evento de `paquete_listo_para_clasificar`. |

## Project Structure

> Se extiende la arquitectura hexagonal. Un listener de mensajería inicia el caso de uso, que es confirmado a través de un endpoint REST por la UI.

```text
frontend/
└── src/
    ├── components/
    │   └── clasificacion/
    │       └── ConfirmarZonaDestino.jsx     [NUEVO — UI para confirmar la zona sugerida]
    └── services/
        └── ClasificacionApiService.js       [NUEVO — HTTP a Spring Boot]

backend/
├── domain/
│   ├── model/
│   │   ├── Paquete.java                     [MODIFICADO — Añadir zonaDestinoId y estado]
│   │   └── ZonaDestino.java                 [NUEVO — Entidad de dominio para zona lógica]
│   ├── external/
│   │   └── ZonaDestinoRepository.java       [NUEVO]
│   └── service/
│       └── CalculoZonaDestinoService.java   [NUEVO — Lógica para determinar la zona]
│
├── application/
│   ├── clasificacion/
│   │   ├── ClasificarPaqueteUseCase.java    [NUEVO — Orquestador de la lógica]
│   │   └── PaqueteListoEvent.java           [NUEVO — Evento de entrada]
│
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   ├── web/
    │   │   │   └── ClasificacionController.java [NUEVO — Endpoint para confirmar]
    │   │   └── messaging/
    │   │       └── PaqueteListoClasificacionSqsListener.java    [NUEVO — Listener SQS para evento de UC-004]
    │   └── out/
    │       └── persistence/
    │           ├── ZonaDestinoJpaAdapter.java   [NUEVO]
    │           └── ZonaDestinoJpaRepository.java [NUEVO]
    └── dto/
        ├── request/
        │   └── ConfirmarZonaRequest.java    [NUEVO]
        └── response/
            └── ClasificacionSugeridaResponse.java [NUEVO]
```

---

## Phase 1: Prerequisitos (verificación)

- [ ] T501 Validar que la tabla `zonas_destino` (con límites geográficos o de CP) exista en la base de datos (vía Flyway).
- [ ] T502 Confirmar que la cola SQS `paquete-listo-clasificar-queue` está configurada.

---

## Phase 2: Dominio de Clasificación — Lógica y Entidades

**Purpose**: Modelar la lógica de negocio para la asignación de zonas de destino de forma aislada.

### Tests del dominio (TDD)

- [ ] T503 [P] Test unitario `CalculoZonaDestinoServiceTest`:
  - `calcularZona()`: dado un paquete con coordenadas, retorna la `ZonaDestino` correcta.
  - `calcularZona()`: lanza una excepción si no se encuentra una zona para las coordenadas.
- [ ] T504 [P] Test unitario `PaqueteTest`:
  - `asignarZonaDestino()`: verifica que el `zonaDestinoId` se asigna y el estado cambia a `LISTO_PARA_DESPACHO`.
  - `asignarZonaDestino()`: no permite la asignación si el estado no es `EN_CLASIFICACION`.

### Implementación del dominio

- [ ] T505 [P] Crear la entidad `ZonaDestino` en `domain/model/`.
- [ ] T506 [P] Modificar `Paquete.java` para incluir `zonaDestinoId` y el método `asignarZonaDestino()`.
- [ ] T507 [P] Implementar `CalculoZonaDestinoService`, que contiene la lógica pura para determinar la zona a partir de las coordenadas del paquete.

---

## Phase 3: Servicio de Aplicación — ClasificarPaqueteUseCase (US5)

**Goal**: Orquestar el proceso de clasificación, desde la sugerencia inicial hasta la confirmación final.

### Tests del servicio (TDD)

- [ ] T508 [P] [US5] `ClasificarPaqueteUseCaseTest` — Sugerencia exitosa:
  - Dado: Un `paqueteId` de un paquete `EN_CLASIFICACION`.
  - Cuando: Se ejecuta `sugerirZonaParaPaquete()`.
  - Entonces: Retorna un `ClasificacionSugeridaResponse` con el ID de la zona calculada.
- [ ] T509 [P] [US5] `ClasificarPaqueteUseCaseTest` — Confirmación exitosa:
  - Dado: Un `paqueteId` y un `zonaDestinoId`.
  - Cuando: Se ejecuta `confirmarClasificacion()`.
  - Entonces: El `Paquete` se actualiza a `LISTO_PARA_DESPACHO` y se guarda.

### Implementación del servicio

- [ ] T510 [P] [US5] Implementar `ClasificarPaqueteUseCase`:
```java
// backend/application/clasificacion/ClasificarPaqueteUseCase.java
@Service
@Transactional
public class ClasificarPaqueteUseCase {

    private final PaqueteRepository paqueteRepository;
    private final ZonaDestinoRepository zonaDestinoRepository;
    private final CalculoZonaDestinoService calculoZonaService;

    // Constructor

    public ClasificacionSugeridaResponse sugerirZonaParaPaquete(UUID paqueteId) {
        Paquete paquete = paqueteRepository.findById(paqueteId)
            .orElseThrow(() -> new PaqueteNotFoundException(paqueteId));
        
        ZonaDestino zonaSugerida = calculoZonaService.calcularZona(paquete.getCoordenadas());
        
        // Opcional: guardar estado intermedio PENDIENTE_CONFIRMACION
        // paquete.marcarComoPendienteDeClasificacion(zonaSugerida.getId());
        // paqueteRepository.guardar(paquete);

        return new ClasificacionSugeridaResponse(paqueteId, zonaSugerida.getId(), zonaSugerida.getNombre());
    }

    public void confirmarClasificacion(UUID paqueteId, UUID zonaDestinoId) {
        Paquete paquete = paqueteRepository.findById(paqueteId)
            .orElseThrow(() -> new PaqueteNotFoundException(paqueteId));
        
        ZonaDestino zona = zonaDestinoRepository.findById(zonaDestinoId)
            .orElseThrow(() -> new ZonaDestinoNotFoundException(zonaDestinoId));

        // Validaciones de negocio (FR-003, FR-004)
        if (!zona.esAptaPara(paquete.getTipoMercancia())) {
            throw new ZonaNoAptaException();
        }

        paquete.asignarZonaDestino(zona.getId());
        paqueteRepository.guardar(paquete);
    }
}
```

---

## Phase 4: Adaptadores de Entrada y Salida

### Adaptadores de Mensajería y REST (Backend)

- [ ] T511 [P] [US5] Implementar `PaqueteListoClasificacionSqsListener` que escuche la cola SQS de `paquete-listo-clasificar-queue`.
    - Al recibir un mensaje, este listener podría invocar `sugerirZonaParaPaquete()` y notificar a la UI (vía WebSockets o simplemente la UI puede sondear).
- [ ] T512 [P] [US5] Crear `ClasificacionController` con un endpoint `POST /api/clasificacion/confirmar` que reciba `ConfirmarZonaRequest` y llame a `confirmarClasificacion()`.
- [ ] T513 [US5] Crear un endpoint `GET /api/clasificacion/sugerencia/{paqueteId}` que la UI pueda llamar para obtener la zona sugerida.

### Aplicación UI (Frontend)

- [ ] T514 [US5] Desarrollar el componente `ConfirmarZonaDestino.jsx`. Al escanear un paquete, llamará al endpoint de sugerencia y mostrará la zona.
- [ ] T515 [US5] El componente tendrá un botón "Confirmar" que enviará la confirmación al backend.

### Adaptadores de Persistencia (Backend)

- [ ] T516 [US5] Implementar `ZonaDestinoJpaAdapter` para interactuar con la tabla `zonas_destino`.

---

## Dependencies & Execution Order

- **Dependencia**: Este plan es iniciado por un evento publicado al final de `MOD1-IP-004`.
- **Orden**:
    1.  **Phase 2 (Dominio)**: Implementar las entidades y la lógica de cálculo.
    2.  **Phase 3 (Servicio)**: Crear el caso de uso que orquesta la sugerencia y confirmación.
    3.  **Phase 4 (Adaptadores)**: Implementar el listener de eventos, el controller REST y la UI.

## Notes

- El flujo exacto de cómo la UI se entera de una nueva sugerencia (polling, WebSockets) debe definirse. Para este plan, se asume un endpoint GET que la UI consume activamente.
- La lógica en `CalculoZonaDestinoService` es clave. Puede empezar con una implementación simple (ej. basada en el nombre de la ciudad) y evolucionar hacia una basada en polígonos geoespaciales.

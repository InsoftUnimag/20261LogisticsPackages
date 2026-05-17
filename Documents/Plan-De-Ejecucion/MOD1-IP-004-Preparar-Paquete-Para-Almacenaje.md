# Implementation Plan: Preparar Paquete para Almacenaje (MOD1-IP-004)

**Date**: 2026-04-16
**Spec**: [Preparar Paquete Para Almacenaje](../Specs/MOD1-UC-004-Preparar-Paquete-Para-Almacenaje.md)

## Summary

Como Almacenista, necesito asignar un paquete a su zona de almacenamiento física en la bodega mediante su UUID. Esto permitirá iniciar su procesamiento interno, actualizar el inventario de la zona y, en caso de éxito, invocar automáticamente la siguiente etapa de clasificación por zona de destino. El sistema, basado en React y Spring Boot con Arquitectura Hexagonal, debe sugerir la zona más adecuada, permitir la corrección de datos físicos si hay discrepancias y manejar casos especiales como paquetes frágiles o dañados.

## Technical Context

| Campo | Valor |
|---|---|
| **Language/Version** | Java 21 (Backend) / JavaScript (React para Frontend) |
| **Primary Dependencies** | Spring Web, Spring Data JPA, Spring Validation, PostgreSQL, Gradle |
| **Storage** | PostgreSQL (control transaccional de zonas y paquetes) |
| **Testing** | JUnit 5, Mockito, Testcontainers (PostgreSQL) |
| **Target Platform** | Servidor Linux para Backend API y Web browser para la UI |
| **Project Type** | Single Web Application (Backend / Web) |
| **Performance Goals** | Actualización de contadores de zona de forma atómica y concurrente. |
| **Constraints** | Bloqueo optimista para evitar que dos almacenistas procesen el mismo paquete simultáneamente. |
| **Scale/Scope** | Flujo de alta frecuencia en el centro de distribución. |
| **Framework** | Spring Boot 3.x (Backend), React (Frontend) |
| **Arquitectura** | Hexagonal (Ports & Adapters) — Backend aislado |
| **Mensajería** | Cola asíncrona (Event-Driven) para notificar `clasificar_paquete` |

## Project Structure

```text
frontend/
└── src/
    ├── components/
    │   └── almacenaje/
    │       └── AsignarZonaForm.jsx      [NUEVO — UI para seleccionar paquete y zona]
    └── services/
        └── AlmacenajeApiService.js      [NUEVO — HTTP a Spring Boot]

backend/
├── build.gradle                             [MODIFICADO — Dependencias si aplica]
├── domain/
│   ├── model/
│   │   ├── Paquete.java                     [MODIFICADO — Añadir zonaAlmacenamientoId]
│   │   └── ZonaAlmacenamiento.java          [NUEVO — Entidad de dominio para zona]
│   ├── exception/
│   │   └── ZonaSaturadaException.java       [NUEVO]
│   ├── external/
│   │   ├── ZonaRepository.java              [NUEVO]
│   │   └── ClasificacionEventPublisher.java [NUEVO]
│   └── service/
│       └── AsignacionZonaService.java       [NUEVO — Lógica de dominio para asignación]
│
├── application/
│   └── almacenaje/
│       └── PrepararAlmacenajeUseCase.java   [NUEVO — Orquestador de la lógica]
│
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── web/
    │   │       └── AlmacenajeController.java [NUEVO — Endpoint para asignar zona]
    │   └── out/
    │       ├── persistence/
    │       │   ├── ZonaJpaAdapter.java       [NUEVO]
    │       │   └── ZonaJpaRepository.java    [NUEVO]
    │       └── messaging/
    │           └── ClasificacionEventAdapter.java [NUEVO — Publicador de eventos]
    └── dto/
        ├── request/
        │   └── AsignarZonaRequest.java      [NUEVO]
        └── response/
            └── AsignacionResponse.java      [NUEVO]
```

---

## Phase 1: Prerequisitos (verificación)

- [ ] T401 Verificar que `build.gradle` tenga las dependencias necesarias.
- [ ] T402 Validar que la tabla `zonas_almacenamiento` exista en la base de datos (vía Flyway).
- [ ] T403 Confirmar conectividad con la base de datos y el sistema de colas SQS.

---

## Phase 2: Dominio de Almacenaje — Lógica y Entidades

**Purpose**: Modelar la lógica de negocio de la asignación de zonas de forma aislada.

### Tests del dominio (TDD)

- [ ] T404 [P] Test unitario `ZonaAlmacenamientoTest`:
  - `agregarPaquete()`: verifica que los contadores (peso, volumen, cantidad) se actualicen correctamente.
  - `agregarPaquete()`: lanza `ZonaSaturadaException` si se excede la capacidad.
  - `puedeAlbergar()`: retorna `false` si un paquete `Peligroso` intenta asignarse a una zona `Normal`.
- [ ] T405 [P] Test unitario `PaqueteTest`:
  - `asignarZona()`: verifica que el estado del paquete cambie a `EN_CLASIFICACION`.

### Implementación del dominio

- [ ] T406 [P] Crear la entidad `ZonaAlmacenamiento` en `domain/model/`:
```java
// backend/domain/model/ZonaAlmacenamiento.java
public class ZonaAlmacenamiento {
    private final UUID id;
    private String nombre;
    private CategoriaZona categoria; // Normal, Delicada, Alto Riesgo
    private double capacidadMaximaPeso;
    private double capacidadMaximaVolumen;
    private int capacidadMaximaPaquetes;
    private double pesoActual;
    private double volumenActual;
    private int paquetesActuales;

    // Constructor y getters

    public boolean puedeAlbergar(Paquete paquete) {
        // Lógica de validación de categoría
    }

    public void agregarPaquete(Paquete paquete) {
        // Lógica para actualizar contadores
    }
}
```
- [ ] T407 [P] Modificar `Paquete.java` para incluir la referencia a la zona y el método de asignación.
- [ ] T408 [P] Implementar `AsignacionZonaService` que contenga la lógica de negocio para sugerir y validar la asignación.

---

## Phase 3: Servicio de Aplicación — PrepararAlmacenajeUseCase (US4)

**Goal**: Orquestar la asignación de zona, interactuando con la capa de dominio y los adaptadores de infraestructura.

### Tests del servicio (TDD)

- [ ] T409 [P] [US4] `PrepararAlmacenajeUseCaseTest` — Asignación exitosa:
  - Dado: Un paquete y una zona compatibles.
  - Cuando: Se ejecuta `prepararParaAlmacenaje()`.
  - Entonces: Se guarda el paquete con el estado actualizado y se publica un evento de clasificación.
- [ ] T410 [P] [US4] `PrepararAlmacenajeUseCaseTest` — Discrepancia de datos:
  - Dado: El request incluye nuevos datos de peso/dimensiones.
  - Cuando: Se ejecuta el caso de uso.
  - Entonces: El paquete se actualiza antes de la asignación y se guarda el historial.
- [ ] T411 [P] [US4] `PrepararAlmacenajeUseCaseTest` — Zona saturada:
  - Dado: La zona no tiene capacidad.
  - Cuando: Se intenta asignar.
  - Entonces: Se sugiere una zona de contingencia.

### Implementación del servicio

- [ ] T412 [P] [US4] Implementar `PrepararAlmacenajeUseCase` con inyección de dependencias:
```java
// backend/application/almacenaje/PrepararAlmacenajeUseCase.java
@Service
@Transactional
public class PrepararAlmacenajeUseCase {

    private final PaqueteRepository paqueteRepository;
    private final ZonaRepository zonaRepository;
    private final AsignacionZonaService asignacionZonaService;
    private final ClasificacionEventPublisher eventPublisher;

    // Constructor

    public AsignacionResponse prepararParaAlmacenaje(AsignarZonaRequest request) {
        Paquete paquete = paqueteRepository.findById(request.getPaqueteId())
            .orElseThrow(() -> new PaqueteNotFoundException());
        
        // FR-004: Actualización por discrepancia
        if (request.tieneDiscrepancias()) {
            paquete.actualizarDatosFisicos(request.getNuevasDimensiones(), request.getNuevoPeso());
            // Registrar en historial...
        }

        ZonaAlmacenamiento zona = zonaRepository.findById(request.getZonaId())
            .orElseThrow(() -> new ZonaNotFoundException());

        // Lógica de asignación usando AsignacionZonaService
        asignacionZonaService.asignarPaqueteAZona(paquete, zona);

        paqueteRepository.guardar(paquete);
        zonaRepository.guardar(zona);

        // FR-009: Invocar siguiente paso
        eventPublisher.publicarPaqueteListoParaClasificar(paquete.getId());

        return new AsignacionResponse(paquete.getId(), paquete.getEstado());
    }
}
```

---

## Phase 4: Adaptadores de Entrada y Salida

### Adaptador REST (Backend)

- [ ] T413 [US4] Crear `AlmacenajeController` con el endpoint `POST /api/almacenaje/asignar-zona`.
- [ ] T414 [US4] Implementar `ControllerAdvice` para manejar `ZonaSaturadaException` y otras excepciones de dominio, devolviendo códigos de error apropiados.

### Aplicación UI (Frontend)

- [ ] T415 [US4] Desarrollar el componente `AsignarZonaForm.jsx` que permita al almacenista buscar un paquete por UUID y ver las zonas sugeridas.
- [ ] T416 [US4] Implementar `AlmacenajeApiService.js` para realizar las llamadas al backend.

### Adaptadores de Persistencia y Mensajería (Backend)

- [ ] T417 [US4] Implementar `ZonaJpaAdapter` para interactuar con la tabla `zonas_almacenamiento`.
- [ ] T418 [US4] Implementar `ClasificacionEventAdapter` para publicar el evento en la cola de mensajería.

---

## Phase N: Polish

- [ ] T419 Añadir tests de integración que cubran el flujo completo desde el controller hasta la base de datos y la cola de mensajería.
- [ ] T420 Realizar pruebas de concurrencia en la actualización de los contadores de la zona.

---

## Dependencies & Execution Order

```text
SPRINT BASE (Spring Init, Java 21 SDK, Vite+React init)
    └── Este Plan: Módulo de Almacenaje
            ├── Phase 2 (Dominio: Entidades y lógica de negocio)
            ├── Phase 3 (Servicio de aplicación orquestador)
            ├── Phase 4 (Controller, UI y adaptadores de persistencia/mensajería)
            └── Phase N (Tests de integración y pulido)
```

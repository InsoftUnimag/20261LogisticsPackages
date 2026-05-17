# Implementation Plan: Actualizar Estado de Paquete por Novedad (MOD1-UC-006)

**Date**: 2026-04-18
**Spec**: [Actualizar Estado de Paquete por Novedad](../Specs/MOD1-UC-006-Actualizar-Estado-De-Disponibilidad.md)

---

## Summary

Como Almacenista, necesito modificar el estado de los paquetes cuando detecte que están dañados o extraviados en la bodega para mantener la trazabilidad actualizada y alertar al Controlador de Novedades. El sistema, basado en una Arquitectura Hexagonal con Spring Boot (Java 21) y React, registrará la transición de estado en un historial inmutable, asociará la evidencia fotográfica obligatoria para daños (almacenada en S3) y notificará al sistema de novedades de forma asíncrona vía Amazon SQS.

---

## Technical Context

| Campo | Valor |
|---|---|
| **Language/Version** | Java 21 (Backend) / JavaScript (React para Frontend) |
| **Primary Dependencies** | Spring Web, Spring Data JPA, Spring Security, Spring Validation, PostgreSQL, Spring Cloud AWS SQS, AWS S3 SDK, Gradle |
| **Storage** | PostgreSQL (para el paquete y su historial de estados) y AWS S3 (o similar) para la evidencia fotográfica. |
| **Testing** | JUnit 5, Mockito, Testcontainers (PostgreSQL, Localstack) |
| **Target Platform** | Servidor Linux para Backend API y Web browser para la UI |
| **Project Type** | Extensión de Single Web Application (Backend / Web) |
| **Performance Goals** | La actualización de estado debe ser una operación rápida (<500ms). La carga de archivos multimedia no debe bloquear la UI. |
| **Constraints** | Evidencia fotográfica obligatoria para novedades de tipo "Dañado". Bloqueo de transiciones de estado inválidas. |
| **Scale/Scope** | Módulo crítico para la trazabilidad y gestión de incidencias del paquete. |
| **Framework** | Spring Boot 3.x (Backend), React (Frontend) |
| **Arquitectura** | Hexagonal (Ports & Adapters) — Backend aislado |
| **Mensajería** | Cola asíncrona (Event-Driven) para notificar `novedad_registrada` |

---

## Project Structure

> Se extiende la arquitectura hexagonal existente. Se añade un nuevo caso de uso, un controller y los adaptadores necesarios para la persistencia, mensajería y almacenamiento de archivos.

```text
frontend/
└── src/
    ├── components/
    │   └── novedad/
    │       └── RegistrarNovedadForm.jsx      [NUEVO — UI para registrar novedad y subir evidencia]
    └── services/
        └── NovedadApiService.js            [NUEVO — HTTP a Spring Boot, incluye subida de archivo]

backend/
├── domain/
│   ├── model/
│   │   ├── Paquete.java                     [MODIFICADO — Añadir método para registrar novedad]
│   │   └── HistorialEstado.java             [NUEVO — Entidad para el historial de estados]
│   ├── exception/
│   │   ├── EstadoTransicionInvalidaException.java [NUEVO]
│   │   └── EvidenciaRequeridaException.java [NUEVO]
│   └── external/
│           ├── HistorialEstadoRepository.java [NUEVO]
│           ├── NovedadEventPublisher.java   [NUEVO — Port para notificar]
│           └── ArchivoStoragePort.java      [NUEVO — Port para guardar evidencia]
│
├── application/
│   └── novedad/
│       ├── RegistrarNovedadUseCase.java    [NUEVO — Orquestador de lógica de negocio]
│       └── RegistrarNovedadCommand.java    [NUEVO]
│
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── web/
    │   │       └── NovedadController.java  [NUEVO — Endpoint para registrar novedad]
    │   └── out/
    │       ├── persistence/
    │       │   ├── HistorialEstadoJpaAdapter.java [NUEVO]
    │       │   └── HistorialEstadoJpaRepository.java [NUEVO]
    │       ├── messaging/
    │       │   └── NovedadEventAdapter.java    [NUEVO — Implementación del publisher]
    │       └── storage/
    │           └── S3ArchivoStorageAdapter.java [NUEVO — Implementación para guardar archivos en S3]
    └── dto/
        ├── request/
        │   └── RegistroNovedadRequest.java [NUEVO — Incluye metadatos y archivo]
        └── response/
            └── RegistroNovedadResponse.java [NUEVO]
```

---

## Phase 1: Prerequisitos (verificación)

- [ ] T601 Verificar que `build.gradle` tenga las dependencias de Spring Cloud AWS SQS y AWS S3 SDK.
- [ ] T602 Validar que la migración de Flyway para la tabla `historial_estados` se ejecute correctamente.
- [ ] T603 Confirmar la configuración de credenciales para AWS (S3 y SQS/SNS) y la base de datos.

---

## Phase 2: Dominio — Lógica de Trazabilidad y Novedades

**Purpose**: Implementar las reglas de negocio para la actualización de estado y el historial, manteniendo el dominio aislado.

### Tests del dominio (TDD)

- [ ] T604 [P] Test unitario `PaqueteNovedadTest`:
  - `registrarNovedad()`: lanza `EstadoTransicionInvalidaException` si el paquete ya está `EN_TRANSITO`.
  - `registrarNovedad()`: lanza `EvidenciaRequeridaException` si la novedad es `DAÑADO` y no se proporciona URL de evidencia.
  - `registrarNovedad()`: actualiza el estado a `NOVEDAD_EN_BODEGA` y crea un `HistorialEstado` si la transición es válida.
- [ ] T605 [P] Test unitario `HistorialEstadoTest`:
  - La creación de una instancia de `HistorialEstado` debe registrar correctamente el estado anterior, el nuevo, el usuario y el timestamp.

### Implementación del dominio

- [ ] T606 [P] Modificar `Paquete.java` para incluir el método de registro de novedad:
```java
// backend/domain/model/Paquete.java
public class Paquete {
    // ... atributos existentes ...
    @Version // Para bloqueo optimista
    private Long version;

    public HistorialEstado registrarNovedad(TipoNovedad tipo, String observaciones, UsuarioId usuarioId, String urlEvidencia) {
        if (!this.estado.permiteNovedadEnBodega()) { // ej. !Arrays.asList(RECIBIDO_EN_SEDE, EN_CLASIFICACION).contains(this.estado)
            throw new EstadoTransicionInvalidaException(this.id, this.estado);
        }
        if (tipo == TipoNovedad.DAÑADO && (urlEvidencia == null || urlEvidencia.isBlank())) {
            throw new EvidenciaRequeridaException(this.id);
        }

        EstadoPaquete estadoAnterior = this.estado;
        this.estado = EstadoPaquete.NOVEDAD_EN_BODEGA;
        // this.subEstado = tipo; // Si se modela como sub-estado

        return new HistorialEstado(this.id, estadoAnterior, this.estado, observaciones, usuarioId, urlEvidencia);
    }
}
```
- [ ] T607 [P] Crear la entidad `HistorialEstado.java` en el dominio.

---

## Phase 3: Servicio de Aplicación — RegistrarNovedadUseCase (US6)

**Goal**: Orquestar el flujo completo: guardar evidencia, actualizar el paquete, registrar el historial y notificar.

### Tests del servicio (TDD)

- [ ] T608 [P] [US6] `RegistrarNovedadUseCaseTest` — Novedad de "Dañado" exitosa:
  - Dado: Un `RegistrarNovedadCommand` con un archivo de evidencia.
  - Cuando: Se ejecuta `registrarNovedad()`.
  - Entonces: `archivoStoragePort.guardar()` es llamado, `paqueteRepository.guardar()` y `historialRepository.guardar()` son llamados, y finalmente `novedadEventPublisher.publicar()` es invocado.
- [ ] T609 [P] [US6] `RegistrarNovedadUseCaseTest` — Falla al guardar archivo:
  - Dado: `archivoStoragePort.guardar()` lanza una excepción.
  - Cuando: Se ejecuta `registrarNovedad()`.
  - Entonces: La transacción se revierte y no se guarda nada en la base de datos ni se publica ningún evento.

### Implementación del servicio

- [ ] T610 [P] [US6] Implementar `RegistrarNovedadUseCase`:
```java
// backend/application/novedad/RegistrarNovedadUseCase.java
@Service
@Transactional
public class RegistrarNovedadUseCase {

    private final PaqueteRepository paqueteRepository;
    private final HistorialEstadoRepository historialRepository;
    private final ArchivoStoragePort archivoStoragePort;
    private final NovedadEventPublisher novedadEventPublisher;

    // Constructor con inyección de dependencias

    public RegistroNovedadResponse registrarNovedad(RegistrarNovedadCommand command) {
        Paquete paquete = paqueteRepository.findById(command.getPaqueteId())
            .orElseThrow(() -> new PaqueteNotFoundException(command.getPaqueteId()));

        String urlEvidencia = null;
        if (command.getEvidencia() != null) {
            urlEvidencia = archivoStoragePort.guardar(
                "novedades", command.getPaqueteId().toString(), command.getEvidencia()
            );
        }

        HistorialEstado historial = paquete.registrarNovedad(
            command.getTipoNovedad(), command.getObservaciones(), command.getUsuarioId(), urlEvidencia
        );

        paqueteRepository.guardar(paquete);
        historialRepository.guardar(historial);

        novedadEventPublisher.publicarNovedadRegistrada(paquete.getId(), historial.getId());

        return new RegistroNovedadResponse(paquete.getId(), paquete.getEstado(), historial.getId());
    }
}
```

---

## Phase 4: Adaptadores de Entrada y Salida

### Adaptador REST (Backend)

- [ ] T611 [US6] Crear `NovedadController` con un endpoint `POST /api/paquetes/{id}/novedades` que acepte `multipart/form-data`.
- [ ] T612 [US6] Implementar `ControllerAdvice` para manejar `EstadoTransicionInvalidaException`, `EvidenciaRequeridaException` y `PaqueteNotFoundException`.

### Aplicación UI (Frontend)

- [ ] T613 [US6] Desarrollar `RegistrarNovedadForm.jsx` con campos para tipo de novedad, observaciones y un campo de subida de archivo.
- [ ] T614 [US6] El formulario debe deshabilitar el botón de guardar si el tipo es "Dañado" y no se ha seleccionado un archivo.

### Adaptadores de Infraestructura (Backend)

- [ ] T615 [US6] Implementar `S3ArchivoStorageAdapter` que use el SDK de AWS S3 para subir el archivo y retornar su URL.
- [ ] T616 [US6] Implementar `NovedadEventAdapter` usando `SqsTemplate` para publicar el evento en la cola SQS correspondiente.
- [ ] T617 [US6] Implementar `HistorialEstadoJpaAdapter` y su `JpaRepository`.

---

## Dependencies & Execution Order

- **Dependencia**: Requiere que un paquete exista en el sistema, típicamente en un estado previo a `EN_TRANSITO`.
- **Orden**:
    1.  **Phase 2 (Dominio)**: Implementar la lógica de negocio y las entidades.
    2.  **Phase 3 (Servicio)**: Crear el caso de uso que orquesta el flujo.
    3.  **Phase 4 (Adaptadores)**: Exponer la funcionalidad a través de la API REST y conectar con los servicios externos (S3, SQS).

## Notes

- El manejo de la concurrencia se abordará con bloqueo optimista (`@Version` en la entidad `Paquete`), que es una estrategia común en los otros planes. Si una actualización falla debido a una modificación concurrente, el `ControllerAdvice` puede manejar la `ObjectOptimisticLockingFailureException` y retornar un código de estado 409 (Conflict).
- La autenticación y autorización para saber qué `UsuarioId` está realizando la operación se obtendrá del contexto de seguridad de Spring Security, que se asume ya configurado.

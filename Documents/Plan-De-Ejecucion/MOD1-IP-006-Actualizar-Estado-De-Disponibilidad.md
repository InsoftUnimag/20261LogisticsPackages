# Implementation Plan: Actualizar Estado de Paquete por Novedad (MOD1-IP-006)

**Date**: 2026-04-18
**Spec**: [Actualizar Estado de Paquete por Novedad](../Specs/MOD1-UC-006-Actualizar-Estado-De-Disponibilidad.md)

## Summary

Como Almacenista, necesito modificar el estado de los paquetes cuando detecte que están dañados o extraviados en la bodega, para mantener la trazabilidad actualizada y alertar al Controlador de Novedades. El sistema, basado en React y Spring Boot con Arquitectura Hexagonal, debe mantener un historial inmutable de todas las transiciones, requerir evidencia multimedia obligatoria para paquetes dañados, notificar automáticamente al Controlador de Novedades y bloquear actualizaciones inválidas según el estado actual del paquete.

## Technical Context

| Campo | Valor |
|---|---|
| **Language/Version** | Java 21 (Backend) / JavaScript (React para Frontend) |
| **Primary Dependencies** | Spring Web, Spring Data JPA, Spring Validation, Spring Boot Starter AMQP / AWS SQS, PostgreSQL, Gradle, AWS S3 / MinIO (almacenamiento de evidencias) |
| **Storage** | PostgreSQL (historial inmutable y estados), S3/MinIO (archivos multimedia) |
| **Testing** | JUnit 5, Mockito, Testcontainers (PostgreSQL, RabbitMQ) |
| **Target Platform** | Servidor Linux para Backend API y Web browser para la UI |
| **Project Type** | Single Web Application (Backend / Web) |
| **Performance Goals** | Actualización de estado < 2s (sin incluir upload de archivos) |
| **Constraints** | Historial inmutable. Bloqueo optimista para evitar actualizaciones concurrentes. Evidencia obligatoria para tipo `Dañado`. |
| **Scale/Scope** | Componente crítico para trazabilidad y gestión de incidencias en bodega. |
| **Framework** | Spring Boot 3.x (Backend), React (Frontend) |
| **Arquitectura** | Hexagonal (Ports & Adapters) — Backend aislado |
| **Mensajería** | Cola asíncrona (Event-Driven) para notificar al `Controlador de Novedades` |

## Project Structure

> Se extiende la estructura hexagonal existente. Los cambios se centran en el historial de estados del `Paquete` y un nuevo caso de uso para registrar novedades.

```text
frontend/
└── src/
    ├── components/
    │   └── novedad/
    │       └── RegistrarNovedadForm.jsx     [NUEVO — UI para registrar novedad]
    └── services/
        └── NovedadApiService.js             [NUEVO — HTTP Fetchs a Spring Boot]

backend/
├── build.gradle                             [MODIFICADO — Dependencias de S3/MinIO]
├── domain/
│   ├── model/
│   │   ├── Paquete.java                     [MODIFICADO — Añadir métodos de transición]
│   │   ├── HistorialEstado.java             [NUEVO — Value Object/Entidad para historial]
│   │   └── NovedadBodega.java               [NUEVO — Entidad de dominio para novedad]
│   ├── valueobject/
│   │   ├── EstadoPaquete.java               [MODIFICADO — Añadir estados de novedad]
│   │   ├── TipoNovedad.java                 [NUEVO — Enum: Dañado, Extraviado]
│   │   └── Evidencia.java                   [NUEVO — Value Object para evidencia multimedia]
│   ├── exception/
│   │   ├── TransicionInvalidaException.java [NUEVO]
│   │   ├── EvidenciaRequeridaException.java [NUEVO]
│   │   └── PaqueteEnTransitoException.java  [NUEVO]
│   ├── external/
│   │   ├── HistorialRepository.java         [NUEVO]
│   │   ├── EvidenciaStorageService.java     [NUEVO — Port para S3/MinIO]
│   │   └── NovedadEventPublisher.java       [NUEVO — Port para notificaciones]
│   └── service/
│       └── ValidadorTransicionEstado.java   [NUEVO — Lógica de validación de transiciones]
│
├── application/
│   └── novedad/
│       ├── RegistrarNovedadUseCase.java     [NUEVO — Orquestador de la lógica]
│       └── NovedadCommand.java              [NUEVO — Command object]
│
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── web/
    │   │       └── NovedadController.java   [NUEVO — Endpoints para registrar novedad]
    │   └── out/
    │       ├── persistence/
    │       │   ├── HistorialJpaAdapter.java [NUEVO]
    │       │   └── HistorialJpaRepository.java [NUEVO]
    │       ├── storage/
    │       │   └── S3EvidenciaAdapter.java  [NUEVO — Implementación S3/MinIO]
    │       └── messaging/
    │           └── NovedadEventAdapter.java [NUEVO — Publisher para notificaciones]
    └── dto/
        ├── request/
        │   └── RegistrarNovedadRequest.java [NUEVO]
        └── response/
            └── NovedadRegistradaResponse.java [NUEVO]
```

---

## Phase 1: Prerequisitos (verificación)

**Purpose**: Validar configuración inicial y dependencias.

- [ ] T601 Verificar que `build.gradle` incluya dependencias de AWS S3 SDK o MinIO Client para almacenamiento de evidencias.
- [ ] T602 Validar que la tabla `historial_estados` exista en la base de datos con estructura inmutable (sin UPDATE, solo INSERT).
- [ ] T603 Confirmar configuración de bucket S3/MinIO para almacenamiento de evidencias multimedia.
- [ ] T604 Verificar configuración de cola de mensajería para notificaciones al Controlador de Novedades.

---

## Phase 2: Dominio del Historial y Novedades — Lógica y Validaciones

**Purpose**: La lógica pura de transiciones de estado y validaciones permanece aislada, sin dependencias de tecnologías externas.

⚠️ **CRÍTICO**: Ningún archivo bajo `domain/` puede contener importaciones de Spring o librerías externas que no sean propias de Java 21.

### Tests del dominio (TDD)

- [ ] T605 [P] Test unitario `HistorialEstadoTest`:
  - Instanciar `new HistorialEstado(...)` genera automáticamente timestamp UTC inmutable.
  - El historial contiene referencia al estado anterior, nuevo estado, usuario responsable y notas.
- [ ] T606 [P] Test unitario `NovedadBodegaTest`:
  - Una novedad de tipo `DAÑADO` requiere al menos una evidencia multimedia.
  - Una novedad de tipo `EXTRAVIADO` no requiere evidencia obligatoria.
- [ ] T607 [P] Test unitario `ValidadorTransicionEstadoTest`:
  - `validarTransicion()`: permite transición de `EN_CLASIFICACION` a `NOVEDAD_EN_BODEGA`.
  - `validarTransicion()`: bloquea transición desde `EN_TRANSITO` o estados posteriores.
  - `validarTransicion()`: lanza `TransicionInvalidaException` para transiciones no permitidas.
- [ ] T608 [P] Test unitario `PaqueteTest`:
  - `registrarNovedad()`: actualiza el estado y agrega entrada al historial.
  - `registrarNovedad()`: lanza excepción si el paquete ya está en tránsito.

### Implementación del dominio

- [ ] T609 [P] Crear entidad `HistorialEstado.java`:
```java
// backend/domain/model/HistorialEstado.java
public class HistorialEstado {
    private final UUID id;
    private final UUID paqueteId;
    private final EstadoPaquete estadoAnterior;
    private final EstadoPaquete estadoNuevo;
    private final LocalDateTime fechaTransicion;
    private final String usuarioResponsable;
    private final String notas;
    private final List<Evidencia> evidencias;

    public HistorialEstado(UUID paqueteId, EstadoPaquete estadoAnterior, 
                          EstadoPaquete estadoNuevo, String usuarioResponsable, 
                          String notas, List<Evidencia> evidencias) {
        this.id = UUID.randomUUID();
        this.paqueteId = paqueteId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.fechaTransicion = LocalDateTime.now(ZoneOffset.UTC);
        this.usuarioResponsable = usuarioResponsable;
        this.notas = notas;
        this.evidencias = evidencias != null ? List.copyOf(evidencias) : List.of();
    }
    
    // Solo getters, sin setters (inmutable)
}
```

- [ ] T610 [P] Crear entidad `NovedadBodega.java`:
```java
// backend/domain/model/NovedadBodega.java
public class NovedadBodega {
    private final UUID id;
    private final UUID paqueteId;
    private final TipoNovedad tipo;
    private final String descripcion;
    private final LocalDateTime fechaDeteccion;
    private final String almacenistaResponsable;
    private final List<Evidencia> evidencias;

    public NovedadBodega(UUID paqueteId, TipoNovedad tipo, String descripcion,
                        String almacenistaResponsable, List<Evidencia> evidencias) {
        this.id = UUID.randomUUID();
        this.paqueteId = paqueteId;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaDeteccion = LocalDateTime.now(ZoneOffset.UTC);
        this.almacenistaResponsable = almacenistaResponsable;
        
        // FR-004: Validar evidencia obligatoria para tipo Dañado
        if (tipo == TipoNovedad.DAÑADO && (evidencias == null || evidencias.isEmpty())) {
            throw new EvidenciaRequeridaException("La novedad de tipo DAÑADO requiere evidencia multimedia.");
        }
        
        this.evidencias = evidencias != null ? List.copyOf(evidencias) : List.of();
    }
    
    // Solo getters
}
```

- [ ] T611 [P] Modificar `Paquete.java` para incluir lógica de transición:
```java
// backend/domain/model/Paquete.java
public class Paquete {
    // ... atributos existentes ...
    
    private EstadoPaquete estado;
    private final List<HistorialEstado> historial = new ArrayList<>();

    public HistorialEstado registrarNovedad(TipoNovedad tipoNovedad, String descripcion,
                                           String usuarioResponsable, List<Evidencia> evidencias) {
        // FR-006: Bloquear si ya está en tránsito o posterior
        if (this.estado.esPosteriorA(EstadoPaquete.EN_CLASIFICACION)) {
            throw new PaqueteEnTransitoException(
                "No se puede registrar novedad en paquete con estado: " + this.estado
            );
        }
        
        EstadoPaquete estadoAnterior = this.estado;
        EstadoPaquete nuevoEstado = EstadoPaquete.NOVEDAD_EN_BODEGA;
        
        // Crear entrada en historial
        HistorialEstado entrada = new HistorialEstado(
            this.id, estadoAnterior, nuevoEstado, usuarioResponsable, 
            descripcion, evidencias
        );
        
        this.historial.add(entrada);
        this.estado = nuevoEstado;
        
        return entrada;
    }
    
    public List<HistorialEstado> getHistorial() {
        return List.copyOf(this.historial); // Retornar copia inmutable
    }
}
```

- [ ] T612 [P] Crear Value Objects `TipoNovedad.java` y `Evidencia.java`:
```java
// backend/domain/valueobject/TipoNovedad.java
public enum TipoNovedad {
    DAÑADO,
    EXTRAVIADO
}

// backend/domain/valueobject/Evidencia.java
public class Evidencia {
    private final String nombreArchivo;
    private final String urlAlmacenamiento;
    private final TipoArchivo tipoArchivo; // FOTO, VIDEO
    private final long tamanioBytes;
    private final LocalDateTime fechaCaptura;

    // Constructor y getters
}
```

- [ ] T613 [P] Implementar `ValidadorTransicionEstado.java`:
```java
// backend/domain/service/ValidadorTransicionEstado.java
public class ValidadorTransicionEstado {

    public void validarTransicion(EstadoPaquete estadoActual, EstadoPaquete estadoNuevo) {
        // FR-006: Validar que no esté en tránsito o posterior
        if (estadoActual.esPosteriorA(EstadoPaquete.EN_CLASIFICACION)) {
            throw new TransicionInvalidaException(
                "No se permite cambiar estado desde: " + estadoActual
            );
        }
        
        // Validar otras reglas de transición según sea necesario
    }
}
```

---

## Phase 3: Servicio de Aplicación — RegistrarNovedadUseCase (US6)

**Goal**: Orquestador de lógica de negocio que registra la novedad, almacena evidencias y notifica.

**Independent Test**: Instanciar `RegistrarNovedadUseCase` usando implementaciones Mock de las dependencias externas.

### Tests del servicio (TDD)

- [ ] T614 [P] [US6] `RegistrarNovedadUseCaseTest` — Registro exitoso de paquete dañado:
  - Dado: Un paquete en estado `EN_CLASIFICACION`, comando con tipo `DAÑADO` y evidencia fotográfica adjunta.
  - Cuando: Se ejecuta `registrarNovedad()`.
  - Entonces: 
    - El paquete cambia a estado `NOVEDAD_EN_BODEGA`.
    - Se guarda entrada en historial con timestamp y usuario.
    - Se almacena evidencia en S3.
    - Se publica evento de notificación al Controlador de Novedades.

- [ ] T615 [P] [US6] `RegistrarNovedadUseCaseTest` — Bloqueo por falta de evidencia:
  - Dado: Comando con tipo `DAÑADO` sin evidencia adjunta.
  - Cuando: Se intenta registrar la novedad.
  - Entonces: Lanza `EvidenciaRequeridaException` y no se guarda nada.

- [ ] T616 [P] [US6] `RegistrarNovedadUseCaseTest` — Registro de paquete extraviado:
  - Dado: Un paquete en estado válido, comando con tipo `EXTRAVIADO`.
  - Cuando: Se ejecuta `registrarNovedad()`.
  - Entonces: Se registra sin requerir evidencia obligatoria.

- [ ] T617 [P] [US6] `RegistrarNovedadUseCaseTest` — Bloqueo por estado inválido:
  - Dado: Un paquete en estado `EN_TRANSITO`.
  - Cuando: Se intenta registrar novedad.
  - Entonces: Lanza `PaqueteEnTransitoException`.

### Implementación del servicio

- [ ] T618 [P] [US6] Implementar caso de uso con inyección de dependencias:
```java
// backend/application/novedad/RegistrarNovedadUseCase.java
@Service
@Transactional
public class RegistrarNovedadUseCase {

    private final PaqueteRepository paqueteRepository;
    private final HistorialRepository historialRepository;
    private final EvidenciaStorageService evidenciaStorage;
    private final NovedadEventPublisher eventPublisher;
    private final ValidadorTransicionEstado validador;

    public RegistrarNovedadUseCase(PaqueteRepository paqueteRepository,
                                  HistorialRepository historialRepository,
                                  EvidenciaStorageService evidenciaStorage,
                                  NovedadEventPublisher eventPublisher,
                                  ValidadorTransicionEstado validador) {
        this.paqueteRepository = paqueteRepository;
        this.historialRepository = historialRepository;
        this.evidenciaStorage = evidenciaStorage;
        this.eventPublisher = eventPublisher;
        this.validador = validador;
    }

    public NovedadRegistradaResponse registrarNovedad(NovedadCommand command) {
        // 1. Obtener el paquete
        Paquete paquete = paqueteRepository.findById(command.getPaqueteId())
            .orElseThrow(() -> new PaqueteNotFoundException(command.getPaqueteId()));

        // 2. Validar transición
        validador.validarTransicion(paquete.getEstado(), EstadoPaquete.NOVEDAD_EN_BODEGA);

        // 3. Procesar y almacenar evidencias (si existen)
        List<Evidencia> evidencias = new ArrayList<>();
        if (command.getArchivosEvidencia() != null && !command.getArchivosEvidencia().isEmpty()) {
            for (var archivo : command.getArchivosEvidencia()) {
                String url = evidenciaStorage.almacenar(archivo, command.getPaqueteId());
                evidencias.add(new Evidencia(
                    archivo.getNombre(),
                    url,
                    archivo.getTipo(),
                    archivo.getTamanio(),
                    LocalDateTime.now(ZoneOffset.UTC)
                ));
            }
        }

        // 4. Registrar la novedad en el dominio (incluye validación de evidencia obligatoria)
        NovedadBodega novedad = new NovedadBodega(
            command.getPaqueteId(),
            command.getTipoNovedad(),
            command.getDescripcion(),
            command.getUsuarioResponsable(),
            evidencias
        );

        // 5. Actualizar estado del paquete y agregar al historial
        HistorialEstado entrada = paquete.registrarNovedad(
            command.getTipoNovedad(),
            command.getDescripcion(),
            command.getUsuarioResponsable(),
            evidencias
        );

        // 6. Persistir cambios
        paqueteRepository.actualizar(paquete);
        historialRepository.guardar(entrada);

        // 7. FR-005: Notificar al Controlador de Novedades
        eventPublisher.publicarNovedadRegistrada(novedad);

        return new NovedadRegistradaResponse(
            paquete.getId(),
            paquete.getEstado(),
            entrada.getId(),
            entrada.getFechaTransicion()
        );
    }
}
```

---

## Phase 4: Adaptadores de Entrada (REST y React)

**Purpose**: Exponer la funcionalidad a través de API REST y consumirla desde el frontend.

### Adaptador REST (Backend)

- [ ] T619 [US6] Crear `NovedadController` con validaciones Spring Bean:
```java
// backend/infrastructure/adapter/in/web/NovedadController.java
@RestController
@RequestMapping("/api/novedades")
public class NovedadController {

    private final RegistrarNovedadUseCase registrarNovedadUseCase;

    @PostMapping("/registrar")
    public ResponseEntity<NovedadRegistradaResponse> registrarNovedad(
            @Valid @RequestBody RegistrarNovedadRequest request,
            @RequestParam(required = false) List<MultipartFile> evidencias) {
        
        NovedadCommand command = NovedadCommand.from(request, evidencias);
        NovedadRegistradaResponse response = registrarNovedadUseCase.registrarNovedad(command);
        
        return ResponseEntity.ok(response);
    }
}
```

- [ ] T620 [US6] Implementar `@ControllerAdvice` para manejar excepciones específicas:
  - `EvidenciaRequeridaException` → HTTP 400 (Bad Request)
  - `PaqueteEnTransitoException` → HTTP 409 (Conflict)
  - `TransicionInvalidaException` → HTTP 422 (Unprocessable Entity)

### Aplicación UI (Frontend React)

- [ ] T621 [US6] Crear componente `RegistrarNovedadForm.jsx`:
  - Campo de búsqueda por UUID del paquete.
  - Selector de tipo de novedad (Dañado / Extraviado).
  - Área de texto para descripción.
  - Componente de upload para archivos multimedia (foto/video).
  - Validación en cliente que requiere evidencia si tipo es "Dañado".
  - Previsualización de archivos adjuntos antes de enviar.

- [ ] T622 [US6] Implementar `NovedadApiService.js`:
```javascript
// frontend/src/services/NovedadApiService.js
export const registrarNovedad = async (paqueteId, tipoNovedad, descripcion, archivos) => {
    const formData = new FormData();
    formData.append('paqueteId', paqueteId);
    formData.append('tipoNovedad', tipoNovedad);
    formData.append('descripcion', descripcion);
    
    archivos.forEach(archivo => {
        formData.append('evidencias', archivo);
    });
    
    const response = await fetch('/api/novedades/registrar', {
        method: 'POST',
        body: formData,
        headers: {
            'Authorization': `Bearer ${getToken()}`
        }
    });
    
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message);
    }
    
    return response.json();
};
```

- [ ] T623 [US6] Implementar manejo de errores en UI:
  - Mostrar mensaje claro cuando falta evidencia obligatoria.
  - Indicar cuando el paquete está en estado inválido para actualización.
  - Mostrar confirmación visual de registro exitoso con timestamp.

---

## Phase 5: Adaptadores de Salida (Persistencia, Storage y Mensajería)

**Purpose**: Implementar la infraestructura de comunicación con sistemas externos.

### Persistencia

- [ ] T624 [US6] Implementar `HistorialJpaAdapter`:
```java
// backend/infrastructure/adapter/out/persistence/HistorialJpaAdapter.java
@Component
public class HistorialJpaAdapter implements HistorialRepository {

    private final HistorialJpaRepository jpaRepository;

    @Override
    public void guardar(HistorialEstado entrada) {
        HistorialEstadoEntity entity = HistorialEstadoMapper.toEntity(entrada);
        jpaRepository.save(entity);
    }

    @Override
    public List<HistorialEstado> obtenerPorPaquete(UUID paqueteId) {
        List<HistorialEstadoEntity> entities = jpaRepository.findByPaqueteIdOrderByFechaTransicionAsc(paqueteId);
        return entities.stream()
            .map(HistorialEstadoMapper::toDomain)
            .toList();
    }
}
```

- [ ] T625 [US6] Crear entity JPA `HistorialEstadoEntity` con índices apropiados:
  - Índice en `paqueteId` para consultas rápidas del historial.
  - Índice en `fechaTransicion` para búsquedas temporales.

### Storage de Evidencias

- [ ] T626 [US6] Implementar `S3EvidenciaAdapter`:
```java
// backend/infrastructure/adapter/out/storage/S3EvidenciaAdapter.java
@Component
public class S3EvidenciaAdapter implements EvidenciaStorageService {

    private final AmazonS3 s3Client;
    
    @Value("${storage.bucket.evidencias}")
    private String bucketName;

    @Override
    public String almacenar(ArchivoEvidencia archivo, UUID paqueteId) {
        String key = generarKey(paqueteId, archivo.getNombre());
        
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(archivo.getContentType());
        metadata.setContentLength(archivo.getTamanio());
        
        try {
            s3Client.putObject(bucketName, key, archivo.getInputStream(), metadata);
            return s3Client.getUrl(bucketName, key).toString();
        } catch (Exception e) {
            throw new StorageException("Error almacenando evidencia", e);
        }
    }
    
    private String generarKey(UUID paqueteId, String nombreArchivo) {
        return String.format("evidencias/%s/%s/%s",
            paqueteId.toString(),
            LocalDate.now().format(DateTimeFormatter.ISO_DATE),
            nombreArchivo
        );
    }
}
```

### Mensajería

- [ ] T627 [US6] Implementar `NovedadEventAdapter`:
```java
// backend/infrastructure/adapter/out/messaging/NovedadEventAdapter.java
@Component
public class NovedadEventAdapter implements NovedadEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publicarNovedadRegistrada(NovedadBodega novedad) {
        try {
            NovedadPayload payload = NovedadPayload.from(novedad);
            String jsonPayload = objectMapper.writeValueAsString(payload);
            
            // FR-005: Notificar al Controlador de Novedades
            rabbitTemplate.convertAndSend(
                "novedades_exchange",
                "novedad.registrada",
                jsonPayload
            );
            
            log.info("Novedad publicada para paquete: {}", novedad.getPaqueteId());
        } catch (JsonProcessingException e) {
            log.error("Error serializando payload de novedad", e);
        }
    }
}
```

---

## Phase 6: Tests de Integración

**Purpose**: Verificar el flujo completo end-to-end.

- [ ] T628 Test de integración con Testcontainers (PostgreSQL + RabbitMQ):
  - Verificar que el registro de novedad persiste correctamente en la base de datos.
  - Verificar que el historial se mantiene inmutable (no hay UPDATEs).
  - Verificar que el evento se publica correctamente en la cola.

- [ ] T629 Test de concurrencia:
  - Simular dos almacenistas intentando actualizar el mismo paquete simultáneamente.
  - Verificar que la primera operación tiene éxito y la segunda recibe un error apropiado.

- [ ] T630 Test de validación de evidencia:
  - Intentar registrar novedad de tipo `DAÑADO` sin evidencia.
  - Verificar que el sistema rechaza la operación.

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Mejoras que afectan múltiples aspectos del sistema.

- [ ] T631 Documentación de API:
  - Generar documentación Swagger/OpenAPI para el endpoint de novedades.
  - Incluir ejemplos de payloads y códigos de error.

- [ ] T632 Auditoría y Logging:
  - Registrar todas las actualizaciones de estado en logs estructurados.
  - Incluir información de usuario, timestamp, y resultado de la operación.

- [ ] T633 Performance:
  - Implementar compresión de imágenes antes del upload a S3.
  - Configurar políticas de retención para evidencias antiguas.

- [ ] T634 Seguridad:
  - Validar tipos MIME de archivos adjuntos.
  - Implementar límites de tamaño de archivo (ej. max 10MB por archivo).
  - Escanear archivos en busca de malware antes del almacenamiento.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Prerequisitos)**: No depende de otros módulos - se puede iniciar tras configuración base.
- **Phase 2 (Dominio)**: Depende de Phase 1 - Define la lógica de negocio pura.
- **Phase 3 (Aplicación)**: Depende de Phase 2 - Orquesta la lógica de dominio.
- **Phase 4 (Adaptadores Entrada)**: Depende de Phase 3 - Expone funcionalidad via REST/UI.
- **Phase 5 (Adaptadores Salida)**: Depende de Phase 2 - Implementa infraestructura externa.
- **Phase 6 (Tests Integración)**: Depende de Phases 2-5 - Valida el flujo completo.
- **Phase N (Polish)**: Depende de todas las anteriores - Mejoras transversales.

### Dependencias con Otros Módulos

- **Dependencia en MOD1-IP-004**: Este caso de uso se ejecuta típicamente cuando un paquete está en bodega (estado `EN_CLASIFICACION` o `LISTO_PARA_DESPACHO`).
- **Dependencia hacia MOD1-UC-007**: Las novedades registradas aquí desencadenan el flujo de gestión de novedades por parte del Controlador.

### Orden de Implementación Interno

1. Implementar modelo de dominio (`HistorialEstado`, `NovedadBodega`, validadores).
2. Implementar caso de uso de aplicación (`RegistrarNovedadUseCase`).
3. Implementar adaptadores de salida (persistencia, storage, mensajería).
4. Implementar adaptadores de entrada (controller REST).
5. Implementar frontend (formulario React).
6. Ejecutar tests de integración.
7. Aplicar mejoras de polish.

---

## Notes

- **Inmutabilidad del historial**: Es crítico que el `HistorialEstado` sea append-only. Nunca se deben actualizar o eliminar registros existentes.
- **Gestión de archivos**: Considerar implementar cleanup automático de uploads fallidos para evitar archivos huérfanos en S3.
- **Notificaciones**: El `Controlador de Novedades` (MOD1-UC-007) debe estar preparado para recibir estos eventos. Verificar contrato del payload.
- **Edge cases de concurrencia**: Implementar bloqueo optimista usando `@Version` en la entidad JPA del `Paquete` para prevenir condiciones de carrera.
- **Validación de estados**: La lógica de transición puede evolucionar. Mantener el `ValidadorTransicionEstado` como un componente independiente facilita cambios futuros.

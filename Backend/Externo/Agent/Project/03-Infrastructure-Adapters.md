# 03 — Infrastructure Adapters

## Catálogo de Endpoints REST

### AdmisionController — `/api/paquetes`

| Método | Ruta | Request | Response | Caso de Uso |
|---|---|---|---|---|
| `POST` | `/api/paquetes/admision` | `RegistroAdmisionRequest` | `RegistroAdmisionResponse { paqueteId }` | `RegistrarAdmisionUseCase` |
| `GET` | `/api/paquetes` | Query: page, size, estado, fechaDesde, fechaHasta | `Page<PaqueteListadoResponse>` | `PaqueteRepository.findAll()` |
| `GET` | `/api/paquetes/{idPaquete}` | Path: idPaquete (UUID) | `ConsultaPaqueteResponse` | `ConsultarPaqueteUseCase` |

**RegistroAdmisionRequest:**
```json
{
  "sedeId": "UUID",
  "direccionDestino": { "direccion", "ciudad", "departamento", "pais" },
  "valorDeclarado": "BigDecimal",
  "metodoPago": "PREPAGO | CONTRA_ENTREGA",
  "remitente": { "tipoDocumento", "numeroDocumento", "nombreCompleto", "telefono", "correoElectronico", "direccion" },
  "destinatario": { "...", "direccion" },
  "tipoMercancia": "ESTANDAR | FRAGIL | PELIGROSO",
  "indicadorFormaIrregular": boolean,
  "peso": Double?, "largo": Double?, "ancho": Double?, "alto": Double?,
  "coordenadasManuales": { "latitud", "longitud" }?
}
```

### PesajeController — `/api/paquetes`

| Método | Ruta | Request | Response | Caso de Uso |
|---|---|---|---|---|
| `POST` | `/api/paquetes/pesaje` | `PesajeRequest` | `PesajeResponseDto` | `ProcesarPesajeUseCase` |
| `GET` | `/api/paquetes/pesaje/health` | — | "Servicio de pesaje operativo" | Health check |

### AlmacenajeController — `/api/paquetes`

| Método | Ruta | Request | Response | Caso de Uso |
|---|---|---|---|---|
| `GET` | `/api/paquetes/{paqueteId}/almacenaje/sugerencia` | Path: paqueteId | `AsignacionZonaResponse` | (lógica en controller) |
| `POST` | `/api/paquetes/{paqueteId}/almacenaje` | `AsignarZonaRequest` | `AsignacionZonaResponse` | `PrepararAlmacenajeUseCase` |

**AsignarZonaRequest:**
```json
{
  "paqueteId": "UUID",
  "zonaId": "UUID",
  "datosDiscrepancia": { "pesoKg", "largoCm", "anchoCm", "altoCm" }?
}
```

### ClasificacionController — `/api/paquetes`

| Método | Ruta | Request | Response | Caso de Uso |
|---|---|---|---|---|
| `GET` | `/api/paquetes/clasificacion/sugerencia/{paqueteId}` | Path: paqueteId | `ClasificacionSugeridaResponseDTO` | `ClasificarPaqueteUseCase.sugerirZona()` |
| `POST` | `/api/paquetes/clasificacion/confirmar` | `ConfirmarZonaRequest` | `ConfirmacionClasificacionResponse` | `ClasificarPaqueteUseCase.confirmarClasificacion()` |

**ConfirmarZonaRequest:**
```json
{ "paqueteId": "UUID", "zonaDestinoId": "UUID" }
```

### NovedadController — `/api/paquetes`

| Método | Ruta | Request | Response | Caso de Uso |
|---|---|---|---|---|
| `POST` | `/api/paquetes/{paqueteId}/novedades` | Multipart: `RegistroNovedadRequest` + `evidencia` (file) | `RegistroNovedadResponseDto` | `RegistrarNovedadUseCase` |

### AuthController — `/api/auth`

| Método | Ruta | Request | Response |
|---|---|---|---|
| `POST` | `/api/auth/login` | `LoginRequest` (username, password) | `JwtResponse { token, username, rol }` |
| `POST` | `/api/auth/register` | `RegisterRequest` (username, password, email, nombreCompleto, rol) | `JwtResponse` |

### ConsultaFinancieraController — `/route`

| Método | Ruta | Request | Response | Caso de Uso |
|---|---|---|---|---|
| `GET` | `/route/{idRoute}/package/{idPaquete}` | Path: idRoute, idPaquete | `ConsultaPaqueteResponse` | `ConsultarEstadoPaqueteUseCase` |

## Tabla de Errores por Operación

### Errores en Admisión
| Error | Código HTTP | Causa | Solución |
|---|---|---|---|
| Sede no encontrada | 404 | `sedeId` inválido | Verificar UUID de sede en seed data |
| Datos inválidos | 400 | Faltan campos obligatorios | Completar todos los `@NotNull` |
| Número documento duplicado | 400 | Ya existe paquete con mismo número de documento | Usar número diferente |

### Errores en Pesaje
| Error | Código HTTP | Causa |
|---|---|---|
| Paquete no encontrado | 404 | UUID no existe |
| Peso negativo o cero | 400 | Valor inválido |
| Dimensiones inválidas | 400 | Valores negativos o cero |
| Mercancía no especificada | 400 | Campo `tipoMercancia` nulo |

### Errores en Almacenaje
| Error | Código HTTP | Causa | Solución |
|---|---|---|---|
| Paquete no encontrado | 404 | UUID inválido | Verificar ID del paquete |
| Zona no encontrada | 404 | zonaId inválido | Usar UUID de zona válido |
| Zona incompatible | 400 | Tipo de mercancía no compatible con categoría de zona | Verificar categoría de zona |
| Zona saturada | 400 | Capacidad máxima alcanzada | Usar otra zona o esperar freeing |
| Paquete ya tiene zona | 400 | Ya tiene zona asignada | No reasignar |

### Errores en Clasificación
| Error | Código HTTP | Causa | Solución |
|---|---|---|---|
| Estado inválido | 400 | Paquete no está en `EN_CLASIFICACION` | Primero asignar zona de almacenamiento |
| Zona no encontrada | 404 | zonaDestinoId inválido | Usar UUID válido |
| Zona incompatible | 400 | La zona no acepta el tipo de mercancía | Elegir zona compatible |
| Zona saturada | 400 | Capacidad máxima alcanzada | Elegir otra zona |

### Errores en Novedad
| Error | Código HTTP | Causa | Solución |
|---|---|---|---|
| Evidencia requerida | 400 | Tipo DAÑADO sin imagen | Adjuntar archivo de evidencia |
| Estado inválido | 400 | Estado no permite novedad | Solo desde bodega |
| Archivo muy grande | 400 | > 10MB | Comprimir imagen |

### Mapa completo de rutas

```mermaid
graph LR
    subgraph SECURED[JWT Required]
        ADM[POST /api/paquetes/admision]
        GET_PKG[GET /api/paquetes]
        GET_PKG_ID[GET /api/paquetes/{id}]
        PES[POST /api/paquetes/pesaje]
        PES_H[GET /api/paquetes/pesaje/health]
        ALM_SUG[GET /api/paquetes/{id}/almacenaje/sugerencia]
        ALM[POST /api/paquetes/{id}/almacenaje]
        CLAS_SUG[GET /api/paquetes/clasificacion/sugerencia/{id}]
        CLAS_CONF[POST /api/paquetes/clasificacion/confirmar]
        NOV[POST /api/paquetes/{id}/novedades]
        FIN[GET /route/{idR}/package/{idP}]
    end
    subgraph PUBLIC[Public]
        LOGIN[POST /api/auth/login]
        REG[POST /api/auth/register]
        SWAGGER[/v3/api-docs/**, /swagger-ui/**]
    end
```

## Datos de Prueba (Seed Data — `V3__seed_data.sql`)

Todos los UUIDs referenciados en los endpoints corresponden a datos semilla cargados por Flyway.

### Sedes
| Nombre | UUID | Ciudad | Tipo |
|---|---|---|---|
| Sede Principal Bogotá | `550e8400-e29b-41d4-a716-446655440001` | Bogotá | PRINCIPAL |
| Sede Auxiliar Medellín | `550e8400-e29b-41d4-a716-446655440002` | Medellín | AUXILIAR |
| Sede Principal Cali | `550e8400-e29b-41d4-a716-446655440003` | Cali | PRINCIPAL |

### Zonas de Destino
| Nombre | UUID | Código |
|---|---|---|
| Zona Destino Norte | `660e8400-e29b-41d4-a716-446655440001` | ZD-NORTE-01 |
| Zona Destino Sur | `660e8400-e29b-41d4-a716-446655440002` | ZD-SUR-01 |
| Zona Destino Centro | `660e8400-e29b-41d4-a716-446655440003` | ZD-CENTRO-01 |
| Zona Destino Fragil Especial | `660e8400-e29b-41d4-a716-446655440004` | ZD-FRAGIL-ESP-01 |
| Zona Destino Peligroso | `660e8400-e29b-41d4-a716-446655440005` | ZD-PELIGROSO-01 |
| Zona Destino Retencion | `660e8400-e29b-41d4-a716-446655440006` | ZD-RETENCION-01 |

### Zonas de Almacenaje
| Nombre | UUID | Código | Categoría |
|---|---|---|---|
| Zona Normal A1 | `770e8400-e29b-41d4-a716-446655440001` | ZA-NORMAL-A1 | NORMAL |
| Zona Normal A2 | `770e8400-e29b-41d4-a716-446655440002` | ZA-NORMAL-A2 | NORMAL |
| Zona Delicada B1 | `770e8400-e29b-41d4-a716-446655440003` | ZA-DELICADA-B1 | DELICADA |
| Zona Delicada B2 | `770e8400-e29b-41d4-a716-446655440004` | ZA-DELICADA-B2 | DELICADA |
| Zona Alto Riesgo C1 | `770e8400-e29b-41d4-a716-446655440005` | ZA-ALTO_RIESGO-C1 | ALTO_RIESGO |
| Zona Retencion D1 | `770e8400-e29b-41d4-a716-446655440006` | ZA-RETENCION-D1 | RETENCION |

## Adaptadores de Persistencia JPA

### Mapeo Puerto → Adaptador → Entidad JPA

| Puerto (Interfaz) | Adaptador | Entity/Dbo | JpaRepository |
|---|---|---|---|
| `PaqueteRepository` | `PaqueteJpaAdapter` | `PaqueteDbo` | `PaqueteJpaRepository` |
| `ZonaAlmacenajeRepository` | `ZonaAlmacenajeJpaAdapter` | `ZonaAlmacenajeDbo` | `ZonaAlmacenajeJpaRepository` |
| `ZonaDestinoRepository` | `ZonaDestinoJpaAdapter` | `ZonaDestinoDbo` | `ZonaDestinoJpaRepository` |
| `UsuarioRepository` | `UsuarioJpaAdapter` | `UsuarioEntity` | `UsuarioJpaRepository` |
| `HistorialEstadoRepository` | `HistorialEstadoJpaAdapter` | `HistorialEstadoEntity` | `HistorialEstadoJpaRepository` |
| `EventoProcesadoRepository` | `EventoProcesadoJpaAdapter` | `EventoProcesadoEntity` | `EventoProcesadoJpaRepository` |

### Mappers (MapStruct)

| Mapper | Conversión |
|---|---|
| `PaqueteMapper` | `Paquete` ↔ `PaqueteDbo` (incluye `Coordenadas` ↔ lat/lon, `Peso` ↔ Double, `Dimensiones` ↔ largo/ancho/alto, `PrecioEnvio` ↔ BigDecimal) |
| `ZonaAlmacenajeMapper` | `ZonaAlmacenaje` ↔ `ZonaAlmacenajeDbo` |
| `ZonaDestinoMapper` | `ZonaDestino` ↔ `ZonaDestinoDbo` |
| `UsuarioMapper` | `Usuario` ↔ `UsuarioEntity` |
| `HistorialEstadoMapper` | `HistorialEstado` ↔ `HistorialEstadoEntity` |
| `PersonaMapper` | `Persona` ↔ `PersonaDbo` |

### Columnas críticas en `PaqueteDbo` (entidad JPA principal)

| Columna DB | Tipo | Mapeo desde Domain |
|---|---|---|
| `estado` | `estado_paquete_enum` (NAMED_ENUM) | `EstadoPaquete` |
| `latitud`, `longitud` | Double | `Coordenadas.latitud()`, `.longitud()` |
| `peso` | Double | `Peso.kilogramos` |
| `largo`, `ancho`, `alto` | Double | `Dimensiones.largoCm`/`anchoCm`/`altoCm` |
| `precio_envio` | BigDecimal | `PrecioEnvio.valor` |
| `remitente_id`, `destinatario_id` | FK → PersonaDbo | `Persona` embeddable mapeado a `@ManyToOne` |

## Servicios Técnicos

### Spring Security + JWT

| Componente | Rol |
|---|---|
| `SecurityConfig` | Deshabilita CSRF, stateless sessions, JWT filter antes de `UsernamePasswordAuthenticationFilter`. Endpoints públicos: `/api/auth/**`, `/v3/api-docs/**`, `/swagger-ui/**`. Todo lo demás requiere autenticación. |
| `JwtTokenProvider` | Genera token con `subject=username`, claim `roles`, firmado HMAC-SHA (base64 secret). Valida y parsea. |
| `JwtAuthenticationFilter` | Extrae Bearer token del header Authorization, valida, carga authorities (`ROLE_<rol>`), setea SecurityContext. |
| `CustomUserDetailsService` | Implementa `UserDetailsService`, busca usuario por username en `UsuarioRepository`. |
| `DataInitializer` | `CommandLineRunner`: crea usuario `admin` (ADMIN) y `operador` (OPERADOR_BODEGA) con passwords por defecto. |

### AWS SQS Messaging

| Adaptador | Cola | Propósito |
|---|---|---|
| `RutaSqsAdapter` (productor) | `${DEV_PREFIX}-solicitar-ruta-queue` | Envía solicitud `SOLICITAR_RUTA` al Módulo Gestión Rutas |
| `RutaSqsListener` (consumidor) | `${DEV_PREFIX}-respuestas-ruta-queue` | Recibe respuesta `RUTA_ASIGNADA` y asigna ruta al paquete |
| `RutaEventSqsListener` (consumidor) | `eventos-paquete-queue` | Recibe eventos de estado de paquete desde M2 vía el DTO polimórfico `EventoPaqueteM2Dto` (Jackson `@JsonTypeInfo`). Enruta por `tipo_evento`, mapea a `EventoRutaDto` mediante `EventoPaqueteM2Mapper` e invoca `ProcesarEventoRutaUseCase`. |
| `EventoPaqueteM2Mapper` (componente) | — | Traduce los 6 tipos M2 (`PAQUETE_EN_TRANSITO`, `PAQUETE_ENTREGADO`, `PARADA_FALLIDA`, `NOVEDAD_GRAVE`, `PARADAS_SIN_GESTIONAR`, `PAQUETE_EXCLUIDO_DESPACHO`) a comandos `EventoRutaDto` de la capa de aplicación. |
| `PaqueteListoClasificacionSqsListener` (consumidor) | `paquete-listo-clasificar-queue` | Recibe eventos de paquete listo para clasificar |
| `NovedadEventAdapter` (productor) | `novedad-registrada-queue` | Publica evento de novedad registrada |

Config:
- Spring Cloud AWS SQS con `DefaultCredentialsProviderChain`
- Colas con prefijo dinámico `${DEV_PREFIX}` para aislar entornos de desarrollo
- Jackson `ObjectMapper` con `JavaTimeModule` para serialización de fechas ISO8601

### AWS (S3 + SQS - complemento)

| Servicio | Adaptador | Propósito |
|---|---|---|
| `S3Template` (Spring Cloud AWS) | `S3ArchivoStorageAdapter` | Guardar evidencia fotográfica de novedades |
| `SqsTemplate` (Spring Cloud AWS) | `RutaSqsAdapter` | Enviar solicitud de ruta `SOLICITAR_RUTA` |
| `SqsTemplate` (Spring Cloud AWS) | `NovedadEventAdapter` | Publicar evento de novedad registrada |
| `S3Client` (AWS SDK) | `S3Service` | Listar buckets (verificación de conectividad) |

### Otros

| Componente | Propósito |
|---|---|
| `CorsConfig` | Permite orígenes configurados (`app.cors.allowed-origins`), métodos GET/POST/PUT/DELETE/OPTIONS, headers Authorization |
| `OpenApiConfig` | OpenAPI 3.0 con security scheme Bearer JWT |
| `RestTemplateConfig` | Timeout configurable (`app.geocoding.timeout-ms`) |
| `GoogleMapsAdapter` | Geocodificación via Google Geocoding API |
| `CoverageAreaAdapter` | Verifica coordenadas dentro de bounding box configurable |
| `DistanceCalculatorAdapter` | Distancia Haversine entre coordenadas |
| `MockNotificacionAdapter` | Mock de notificaciones SMS/Email (solo logs) |

## GlobalExceptionHandler — Mapa de errores HTTP

| Excepción | HTTP Status | Código Interno |
|---|---|---|
| `PaqueteNotFoundException` | 404 | `PAQUETE_NO_ENCONTRADO` |
| `EstadoTransicionInvalidaException` | 400 | `TRANSICION_INVALIDA` |
| `EvidenciaRequeridaException` | 400 | `EVIDENCIA_REQUERIDA` |
| `ObjectOptimisticLockingFailureException` | 409 | `CONFLICTO_CONCURRENCIA` |
| `BadCredentialsException` | 401 | `CREDENCIALES_INVALIDAS` |
| `NoResourceFoundException` | 404 | `RECURSO_NO_ENCONTRADO` |
| `MethodArgumentNotValidException` | 400 | `ERROR_VALIDACION` |
| `IllegalArgumentException` | 400 | `ARGUMENTO_INVALIDO` |
| `IllegalStateException` | 409 | `ESTADO_INVALIDO` |
| `ZonaNoAptaException` | 400 | `ZONA_NO_APTA` |
| `ZonaSaturadaException` | 409 | `ZONA_SATURADA` |
| `ZonaAlmacenajeNotFoundException` | 404 | `ZONA_ALMACENAJE_NO_ENCONTRADA` |
| `ZonaDestinoNotFoundException` | 404 | `ZONA_DESTINO_NO_ENCONTRADA` |
| `CoordenadasInvalidasException` | 400 | `COORDENADAS_INVALIDAS` |
| `EventoDuplicadoException` | 409 | `EVENTO_DUPLICADO` |
| `InvalidCoverageException` | 400 | `COBERTURA_INVALIDA` |
| Genérica `Exception` | 500 | `ERROR_INTERNO` |


---

---

---

---

---

## Anexo: Estado de Archivos Actual (infrastructure)
*Generado automáticamente por sync-agent-docs.py el 2026-05-18 07:01:05 UTC*

| Indicador | Valor |
|---|---|
| Clases | 68 |
| Interfaces | 11 |
| Enumeraciones | 2 |
| Records | 0 |
| Métodos públicos (significativos) | 39 |
| Archivos analizados | 82 |

### Tipos Detectados

| Tipo | Nombre | Paquete | Métodos públicos |
|---|---|---|---|
| 🟦 Cls | `CoverageAreaAdapter` | `com.logistics.packages.infrastructure.adapter.external` | `—` |
| 🟦 Cls | `DistanceCalculatorAdapter` | `com.logistics.packages.infrastructure.adapter.external` | `calcularDistanciaKm, calcularDistanciaDesdeSede` |
| 🟦 Cls | `GoogleMapsAdapter` | `com.logistics.packages.infrastructure.adapter.external` | `verifyApiKey` |
| 🟦 Cls | `EventoPaqueteM2Mapper` | `com.logistics.packages.infrastructure.adapter.messaging` | `—` |
| 🟦 Cls | `NovedadEventAdapter` | `com.logistics.packages.infrastructure.adapter.messaging` | `publicarNovedadRegistrada` |
| 🟦 Cls | `PaqueteListoClasificacionSqsListener` | `com.logistics.packages.infrastructure.adapter.messaging` | `procesarPaqueteListoParaClasificacion` |
| 🟦 Cls | `RutaEventAdapter` | `com.logistics.packages.infrastructure.adapter.messaging` | `publicarSolicitudRuta` |
| 🟦 Cls | `RutaEventSqsListener` | `com.logistics.packages.infrastructure.adapter.messaging` | `onEventoPaquete` |
| 🟦 Cls | `RutaSqsAdapter` | `com.logistics.packages.infrastructure.adapter.messaging` | `enviarSolicitud` |
| 🟦 Cls | `RutaSqsListener` | `com.logistics.packages.infrastructure.adapter.messaging` | `recibirRespuesta` |
| 🟦 Cls | `MockNotificacionAdapter` | `com.logistics.packages.infrastructure.adapter.notification` | `enviarSms, enviarEmail, enviar` |
| 🟦 Cls | `EventoProcesadoEntity` | `com.logistics.packages.infrastructure.adapter.persistence.eventoprocesado` | `—` |
| 🟦 Cls | `EventoProcesadoJpaAdapter` | `com.logistics.packages.infrastructure.adapter.persistence.eventoprocesado` | `guardar, yaFueProcesado` |
| 🟩 Int | `EventoProcesadoJpaRepository` | `com.logistics.packages.infrastructure.adapter.persistence.eventoprocesado` | `—` |
| 🟦 Cls | `HistorialEstadoEntity` | `com.logistics.packages.infrastructure.adapter.persistence.historial` | `—` |
| 🟦 Cls | `HistorialEstadoJpaAdapter` | `com.logistics.packages.infrastructure.adapter.persistence.historial` | `guardar` |
| 🟩 Int | `HistorialEstadoJpaRepository` | `com.logistics.packages.infrastructure.adapter.persistence.historial` | `—` |
| 🟦 Cls | `HistorialEstadoMapper` | `com.logistics.packages.infrastructure.adapter.persistence.historial` | `toEntity, toDomain` |
| 🟦 Cls | `PaqueteDbo` | `com.logistics.packages.infrastructure.adapter.persistence.paquete` | `—` |
| 🟦 Cls | `PaqueteJpaAdapter` | `com.logistics.packages.infrastructure.adapter.persistence.paquete` | `save` |
| 🟩 Int | `PaqueteJpaRepository` | `com.logistics.packages.infrastructure.adapter.persistence.paquete` | `—` |
| 🟩 Int | `PaqueteMapper` | `com.logistics.packages.infrastructure.adapter.persistence.paquete` | `—` |
| 🟦 Cls | `PersonaDbo` | `com.logistics.packages.infrastructure.adapter.persistence.persona` | `—` |
| 🟩 Int | `PersonaMapper` | `com.logistics.packages.infrastructure.adapter.persistence.persona` | `—` |
| 🟦 Cls | `UsuarioEntity` | `com.logistics.packages.infrastructure.adapter.persistence.usuario` | `—` |
| 🟦 Cls | `UsuarioJpaAdapter` | `com.logistics.packages.infrastructure.adapter.persistence.usuario` | `save, existsByUsername` |
| 🟩 Int | `UsuarioJpaRepository` | `com.logistics.packages.infrastructure.adapter.persistence.usuario` | `—` |
| 🟩 Int | `UsuarioMapper` | `com.logistics.packages.infrastructure.adapter.persistence.usuario` | `—` |
| 🟦 Cls | `ZonaAlmacenajeDbo` | `com.logistics.packages.infrastructure.adapter.persistence.zonaalmacenaje` | `—` |
| 🟦 Cls | `ZonaAlmacenajeJpaAdapter` | `com.logistics.packages.infrastructure.adapter.persistence.zonaalmacenaje` | `save` |
| 🟩 Int | `ZonaAlmacenajeJpaRepository` | `com.logistics.packages.infrastructure.adapter.persistence.zonaalmacenaje` | `—` |
| 🟩 Int | `ZonaAlmacenajeMapper` | `com.logistics.packages.infrastructure.adapter.persistence.zonaalmacenaje` | `—` |
| 🟦 Cls | `ZonaDestinoDbo` | `com.logistics.packages.infrastructure.adapter.persistence.zonadestino` | `—` |
| 🟦 Cls | `ZonaDestinoJpaAdapter` | `com.logistics.packages.infrastructure.adapter.persistence.zonadestino` | `save` |
| 🟩 Int | `ZonaDestinoJpaRepository` | `com.logistics.packages.infrastructure.adapter.persistence.zonadestino` | `—` |
| 🟩 Int | `ZonaDestinoMapper` | `com.logistics.packages.infrastructure.adapter.persistence.zonadestino` | `—` |
| 🟦 Cls | `S3ArchivoStorageAdapter` | `com.logistics.packages.infrastructure.adapter.storage` | `guardar` |
| 🟦 Cls | `CorsConfig` | `com.logistics.packages.infrastructure.config` | `addCorsMappings` |
| 🟦 Cls | `OpenApiConfig` | `com.logistics.packages.infrastructure.config` | `logisticsPackagesOpenAPI` |
| 🟦 Cls | `RestTemplateConfig` | `com.logistics.packages.infrastructure.config` | `restTemplate` |
| 🟦 Cls | `AdmisionController` | `com.logistics.packages.infrastructure.controller` | `—` |
| 🟦 Cls | `AlmacenajeController` | `com.logistics.packages.infrastructure.controller` | `—` |
| 🟦 Cls | `AuthController` | `com.logistics.packages.infrastructure.controller` | `—` |
| 🟦 Cls | `ClasificacionController` | `com.logistics.packages.infrastructure.controller` | `—` |
| 🟦 Cls | `ConsultaFinancieraController` | `com.logistics.packages.infrastructure.controller` | `—` |
| 🟦 Cls | `GlobalExceptionHandler` | `com.logistics.packages.infrastructure.controller` | `—` |
| 🟦 Cls | `NovedadController` | `com.logistics.packages.infrastructure.controller` | `—` |
| 🟦 Cls | `PesajeController` | `com.logistics.packages.infrastructure.controller` | `—` |
| 🟦 Cls | `ApiError` | `com.logistics.packages.infrastructure.dto` | `—` |
| 🔷 Abs | `EventoPaqueteM2Dto` | `com.logistics.packages.infrastructure.dto.event` | `—` |
| 🟨 Enm | `TipoNovedadGrave` | `com.logistics.packages.infrastructure.dto.event` | `NovedadGraveEvento` |
| 🟦 Cls | `PaqueteEnTransitoEvento` | `com.logistics.packages.infrastructure.dto.event` | `—` |
| 🟦 Cls | `PaqueteEntregadoEvento` | `com.logistics.packages.infrastructure.dto.event` | `—` |
| 🟦 Cls | `PaqueteExcluidoDespachoEvento` | `com.logistics.packages.infrastructure.dto.event` | `—` |
| 🟨 Enm | `MotivoParadaFallida` | `com.logistics.packages.infrastructure.dto.event` | `ParadaFallidaEvento` |
| 🟦 Cls | `ParadasSinGestionarEvento` | `com.logistics.packages.infrastructure.dto.event` | `—` |
| 🟦 Cls | `AsignarZonaRequest` | `com.logistics.packages.infrastructure.dto.request` | `tieneDiscrepancias` |
| 🟦 Cls | `ConfirmarZonaRequest` | `com.logistics.packages.infrastructure.dto.request` | `—` |
| 🟦 Cls | `DatosFisicosDiscrepanciaDto` | `com.logistics.packages.infrastructure.dto.request` | `—` |
| 🟦 Cls | `LoginRequest` | `com.logistics.packages.infrastructure.dto.request` | `—` |
| 🟦 Cls | `PesajeRequest` | `com.logistics.packages.infrastructure.dto.request` | `—` |
| 🟦 Cls | `RegisterRequest` | `com.logistics.packages.infrastructure.dto.request` | `—` |
| 🟦 Cls | `RegistroAdmisionRequest` | `com.logistics.packages.infrastructure.dto.request` | `—` |
| 🟦 Cls | `RegistroNovedadRequest` | `com.logistics.packages.infrastructure.dto.request` | `—` |
| 🟦 Cls | `SolicitudRutaPayload` | `com.logistics.packages.infrastructure.dto.request` | `—` |
| 🟦 Cls | `AsignacionZonaResponse` | `com.logistics.packages.infrastructure.dto.response` | `—` |
| 🟦 Cls | `ClasificacionSugeridaResponseDTO` | `com.logistics.packages.infrastructure.dto.response` | `—` |
| 🟦 Cls | `ConfirmacionClasificacionResponse` | `com.logistics.packages.infrastructure.dto.response` | `—` |
| 🟦 Cls | `ConsultaPaqueteResponse` | `com.logistics.packages.infrastructure.dto.response` | `—` |
| 🟦 Cls | `JwtResponse` | `com.logistics.packages.infrastructure.dto.response` | `—` |
| 🟦 Cls | `PaqueteListadoResponse` | `com.logistics.packages.infrastructure.dto.response` | `fromDomain` |
| 🟦 Cls | `PesajeResponseDto` | `com.logistics.packages.infrastructure.dto.response` | `agregarAlertaSiAplica` |
| 🟦 Cls | `RegistroAdmisionResponse` | `com.logistics.packages.infrastructure.dto.response` | `—` |
| 🟦 Cls | `RegistroNovedadResponseDto` | `com.logistics.packages.infrastructure.dto.response` | `—` |
| 🟦 Cls | `RespuestaRutaPayload` | `com.logistics.packages.infrastructure.dto.response` | `—` |
| 🟦 Cls | `SqsCommunicationException` | `com.logistics.packages.infrastructure.exception` | `—` |
| 🟦 Cls | `CustomUserDetailsService` | `com.logistics.packages.infrastructure.security` | `loadUserByUsername` |
| 🟦 Cls | `DataInitializer` | `com.logistics.packages.infrastructure.security` | `run` |
| 🟦 Cls | `JwtAuthenticationFilter` | `com.logistics.packages.infrastructure.security` | `—` |
| 🟦 Cls | `JwtTokenProvider` | `com.logistics.packages.infrastructure.security` | `generateToken, validateToken` |
| 🟦 Cls | `SecurityConfig` | `com.logistics.packages.infrastructure.security` | `filterChain, authenticationManager, passwordEncoder` |
| 🟦 Cls | `S3Service` | `com.logistics.packages.infrastructure.services` | `listBuckets` |

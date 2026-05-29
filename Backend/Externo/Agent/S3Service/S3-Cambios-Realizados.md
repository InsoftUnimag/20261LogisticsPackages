# Cambios Realizados — Integración Amazon S3 (MOD1-UC-006 FR-004)

## Puerto de almacenamiento (`ArchivoStoragePort`)

Se extendió el puerto hexagonal en `application/repository/ArchivoStoragePort.java` con 3 nuevos métodos:

| Método | Firma | Propósito |
|--------|-------|-----------|
| `eliminar` | `(String carpeta, String identificador)` | Elimina todas las evidencias de un identificador |
| `obtenerUrlFirmada` | `(String carpeta, String identificador, int duracionMinutos)` | Genera URL prefirmada de descarga |
| `obtenerBytes` | `(String carpeta, String identificador)` | Obtiene el contenido binario del archivo |

Método preexistente: `guardar(String carpeta, String identificador, MultipartFile archivo)`.

### Estrategia de resolución de clave S3

- `guardar()` genera la clave con timestamp: `{carpeta}/{id}/{timestamp}-{id}.{ext}`
- Las operaciones de lectura (`eliminar()`, `obtenerUrlFirmada()`, `obtenerBytes()`) usan `listObjectsV2` con prefijo `{carpeta}/{id}/` para encontrar la clave exacta

---

## Adapter S3 (`S3ArchivoStorageAdapter`)

Implementación completa en `infrastructure/adapter/storage/S3ArchivoStorageAdapter.java`.

- Usa `S3Template` de Spring Cloud AWS para subir archivos
- Usa `S3Client` de AWS SDK para listar, eliminar y leer objetos
- Usa `S3Presigner` para generar URLs firmadas (se crea manualmente, no hay auto-config)
- Soporta `endpointOverride` para LocalStack (se inyecta via `spring.cloud.aws.s3.endpoint`)

---

## Casos de uso modificados

### `RegistrarAdmisionUseCase`
- Inyecta `ArchivoStoragePort`
- Guarda evidencia en `"admision"` tras persistir el paquete
- La URL de evidencia se registra en el historial de transición

### `ProcesarPesajeUseCase`
- Inyecta `ArchivoStoragePort` y `HistorialEstadoRepository`
- Guarda evidencia en `"pesaje"` tras persistir el paquete
- Registra transición de pesaje en historial con URL de evidencia

### `ClasificarPaqueteUseCase`
- Inyecta `ArchivoStoragePort`
- Guarda evidencia en `"clasificacion"` antes de asignar zona de destino
- Registra transición en historial con URL de evidencia

### `RegistrarNovedadUseCase`
- Ya tenía `ArchivoStoragePort` previamente (sin cambios)

---

## Controladores cambiados a `multipart/form-data`

Los siguientes endpoints **cambiaron de `@RequestBody` JSON a `@RequestParam` + `MultipartFile`**:

| Endpoint | Controlador | Parámetros |
|----------|------------|------------|
| `POST /api/paquetes/admision` | `AdmisionController` | `sedeId`, `...` (todos los campos JSON) + `evidencia` (file opcional) |
| `POST /api/paquetes/pesaje` | `PesajeController` | `paqueteId`, `peso`, `dimensiones`, `tipoMercancia`, ... + `evidencia` (file opcional) |
| `POST /api/paquetes/clasificacion/confirmar` | `ClasificacionController` | `paqueteId`, `zonaDestinoId` + `evidencia` (file opcional) |

**IMPORTANTE**: Esto es un **cambio rompiente (breaking change)**. El frontend debe migrar de JSON a `FormData`.

---

## Configuración por perfil

### `application.yml` (base)
```yaml
aws:
  s3:
    bucket-name: logistics-packages
    auto-create-bucket: false
spring:
  cloud:
    aws:
      region:
        static: us-east-2
```

### `application-local.yml` (LocalStack)
```yaml
spring:
  cloud:
    aws:
      s3:
        endpoint: http://localhost:4566
aws:
  s3:
    bucket-name: logistics-packages-local
    auto-create-bucket: true
```

### `application-aws.yml` (producción)
```yaml
spring:
  cloud:
    aws:
      s3:
        region: us-east-2
        path-style-access-enabled: false
aws:
  s3:
    bucket-name: logistics-packages-prod
```

### `application.properties` (variable de entorno)
```properties
AWS_S3_BUCKET_NAME=${aws.s3.bucket-name}
```

---

## Auto-creación de bucket (`S3BucketInitializer`)

- `ApplicationRunner` que verifica existencia del bucket al arrancar
- Solo se ejecuta si `aws.s3.auto-create-bucket=true`
- Idempotente: si el bucket ya existe, no falla
- Atrapa errores de permisos (producción) sin bloquear el startup

---

## Tests

### Unitarios
- `ProcesarPesajeUseCaseTest`: Todos pasan ✅ (se agregaron mocks faltantes)
- `ClasificarPaqueteUseCaseTest`: 1 fallo pre-existente (validación de zona no apta eliminada en commit anterior)
- `RegistrarAdmisionUseCaseTest`: Fallos pre-existentes (faltan mocks de `HistorialEstadoRepository` y `SedeRepository`)

### Integración
- `S3ArchivoStorageAdapterIntegrationTest`: 7 tests que requieren Docker + LocalStack
- Configuración via `TestcontainersConfigurationForS3IT` (solo S3, sin PostgreSQL)

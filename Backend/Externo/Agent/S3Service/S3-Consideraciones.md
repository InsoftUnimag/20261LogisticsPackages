# Consideraciones Técnicas — Almacenamiento S3

## 1. Estrategia de nombrado de objetos S3

```
{carpeta}/{identificador}/{timestamp}-{identificador}.{extension}
```

Ejemplo:
```
admision/550e8400-e29b-41d4-a716-446655440000/20260529-143022-550e8400-e29b-41d4-a716-446655440000.jpg
```

- `carpeta`: `admision`, `pesaje`, `clasificacion` o `novedades`
- `identificador`: UUID del paquete
- `timestamp`: formato `yyyyMMdd-HHmmss` para evitar colisiones
- `extension`: extraída del nombre original del archivo

### ⚠️ Limitación conocida

Las operaciones de lectura (`eliminar`, `obtenerUrlFirmada`, `obtenerBytes`) usan `listObjectsV2` con prefijo para ubicar la clave exacta. Esto tiene 2 implicaciones:

1. **Rendimiento**: Si un mismo identificador tiene muchas fotos, `listObjectsV2` puede ser lento al paginar. En la práctica, esperamos 1-3 fotos por paquete.
2. **Consistencia**: `listObjectsV2` es **eventualmente consistente** en S3 estándar. Inmediatamente después de `guardar()`, `listObjectsV2` podría no encontrar el objeto todavía. En LocalStack es fuertemente consistente.

### Recomendación futura

Si la latencia o consistencia son críticas, cambiar a un esquema donde el adapter devuelva la clave S3 exacta al guardar y las operaciones de lectura la reciban como parámetro:

```java
// En lugar de:
String url = archivoStoragePort.guardar(carpeta, identificador, archivo);
archivoStoragePort.eliminar(carpeta, identificador);

// Hacer:
String claveS3 = archivoStoragePort.guardar(carpeta, identificador, archivo);
archivoStoragePort.eliminarPorClave(claveS3);
```

---

## 2. Presigner (URLs firmadas)

`S3Presigner` se construye manualmente en el adapter porque Spring Cloud AWS no auto-configura este componente.

```java
private S3Presigner obtenerPresigner() {
    if (presigner == null) {
        var builder = S3Presigner.builder()
                .region(Region.of(awsRegion));
        if (s3Endpoint != null && !s3Endpoint.isBlank()) {
            builder.endpointOverride(URI.create(s3Endpoint));
        }
        presigner = builder.build();
    }
    return presigner;
}
```

- En **LocalStack**: se usa `endpointOverride` apuntando a `http://localhost:4566`
- En **producción**: no hay `endpointOverride`, AWS SDK resuelve automáticamente el endpoint correcto para la región

---

## 3. Auto-creación de bucket

`S3BucketInitializer` usa `ApplicationRunner` para verificar/crear el bucket al arrancar.

| Perfil | `auto-create-bucket` | Comportamiento |
|--------|---------------------|----------------|
| `local` | `true` | Crea `logistics-packages-local` en LocalStack |
| `test` | `true` | Crea bucket via Testcontainers |
| `aws` (prod) | `false` (default) | No intenta crear; el bucket debe existir previamente |

### Seguridad en producción

En producción, las credenciales IAM pueden no tener permiso `s3:CreateBucket` o `s3:HeadBucket`. El initializer atrapa `S3Exception` y loguea una advertencia sin detener el arranque.

---

## 4. Evidencia opcional

La evidencia es **opcional** en todos los endpoints. El servidor verifica `!= null && !isEmpty()` antes de llamar a S3.

- Si NO se envía archivo: `urlEvidencia = null`
- Si se envía un archivo vacío: se trata como `null`
- Si S3 falla: la excepción se propaga y la transacción hace rollback (via `@Transactional`)

### Casos por use case

| Use Case | ¿Evidencia obligatoria? | Notas |
|----------|------------------------|-------|
| Admisión | No | Foto opcional del paquete al recibirlo |
| Pesaje | No | Foto opcional de la báscula |
| Clasificación | No | Foto opcional de la zona asignada |
| Novedad DAÑADO | Sí | Validado por el dominio (`EvidenciaRequeridaException`) |
| Novedad EXTRAVIADO | No | Sin foto requerida |

---

## 5. Breaking changes

### Controladores

Los 3 controladores ahora usan `consumes = MediaType.MULTIPART_FORM_DATA_VALUE` en lugar de `application/json`.

**Endpoints afectados:**
- `POST /api/paquetes/admision`
- `POST /api/paquetes/pesaje`
- `POST /api/paquetes/clasificacion/confirmar`

### `RegistroAdmisionRequest`

Se agregó `@Setter` y `@NoArgsConstructor` para que Spring pueda mapear los campos de `multipart/form-data` a un DTO. El uso de records con `@RequestParam` no es posible porque `@RequestParam` no admite records con múltiples campos anidados.

### Implicaciones para pruebas manuales (Swagger/Postman)

En Swagger UI, los endpoints de `multipart/form-data` muestran campos de archivo y text input. En Postman, usar `Body > form-data` en lugar de `Body > raw > JSON`.

---

## 6. Configuración de LocalStack

Para desarrollo local, el docker-compose de LocalStack expone S3 en el puerto 4566:

```yaml
services:
  localstack:
    image: localstack/localstack:3.4.0
    ports:
      - "4566:4566"
    environment:
      - SERVICES=sqs,s3
```

Si el equipo de infraestructura ya tiene LocalStack centralizado, solo se necesita asegurar que:
1. El puerto 4566 sea accesible
2. El servicio `s3` esté habilitado en LocalStack
3. (Opcional) Se configure `S3_SKIP_SIGNATURE_VALIDATION=1` para desarrollo

---

## 7. AWS SDK vs Spring Cloud AWS

| Componente | SDK usado | Origen |
|------------|-----------|--------|
| Subida de archivos | `S3Template.upload()` | Spring Cloud AWS |
| Listar/Eliminar/Leer objetos | `S3Client.*` | AWS SDK v2 |
| URLs firmadas | `S3Presigner.*` | AWS SDK v2 |

`S3Template` y `S3Client` son auto-configurados por `S3AutoConfiguration`. `S3Presigner` se construye manualmente.

---

## 8. Tests de integración

### Prerrequisitos
- Docker instalado y corriendo
- La imagen `localstack/localstack:3.4.0` disponible

### Tests disponibles
| Test | Descripción |
|------|-------------|
| `debeGuardarYRetornarUrl()` | Sube archivo, verifica URL |
| `debeEliminarArchivoS3()` | Sube, elimina, verifica que ya no existe |
| `debeGenerarUrlFirmada()` | Sube, genera URL prefirmada |
| `debeObtenerBytes()` | Sube, descarga bytes |
| `debeLanzarExcepcionAlObtenerBytesDeArchivoInexistente()` | Error path |
| `debeLanzarExcepcionAlGenerarUrlFirmadaArchivoInexistente()` | Error path |
| `debeLanzarExcepcionAlObtenerBytesArchivoInexistente()` | Error path |

### Ejecución
```bash
cd Backend
./gradlew test --tests "com.logistics.packages.infrastructure.adapter.storage.S3ArchivoStorageAdapterIntegrationTest"
```

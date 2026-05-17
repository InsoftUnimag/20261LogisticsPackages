# Contrato de API (OpenAPI / Swagger)

## Ubicación de la Documentación Interactiva

Una vez el backend esté corriendo, puedes acceder a la documentación Swagger en:

```
http://localhost:8080/swagger-ui.html
```

También disponible el spec OpenAPI en JSON:

```
http://localhost:8080/v3/api-docs
```

---

## Endpoints Detallados

> **Nota sobre zona horaria:** Todas las fechas en el sistema se manejan estrictamente en **UTC** y se transmiten en formato ISO-8601 (ej. `2026-05-12T05:00:00Z`). El frontend debe convertir a la zona horaria local solo para visualización, pero siempre enviar y recibir en UTC.

### 1. Autenticación (Público — No requiere token)

#### `POST /api/auth/login`
Iniciar sesión y obtener token JWT.

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response 200 OK:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "admin",
  "rol": "ADMIN"
}
```

**Response 401 Unauthorized:**
```json
{
  "codigo": "CREDENCIALES_INVALIDAS",
  "mensaje": "Usuario o contraseña incorrectos",
  "status": 401,
  "timestamp": "2026-05-12T..."
}
```

---

#### `POST /api/auth/register`
Registrar un nuevo usuario.

**Request Body:**
```json
{
  "username": "nuevo_operador",
  "password": "password123",
  "email": "operador@logistics.com",
  "nombreCompleto": "Juan Pérez",
  "rol": "OPERADOR_BODEGA"
}
```

> `rol` es opcional. Por defecto: `OPERADOR_BODEGA`. Valores: `ADMIN`, `OPERADOR_BODEGA`.

**Response 201 Created:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "nuevo_operador",
  "rol": "OPERADOR_BODEGA"
}
```

---

### 2. Gestión de Paquetes (Requiere JWT)

#### `GET /api/paquetes`
Listar paquetes con paginación y filtros opcionales.

**Query Parameters:**

| Parámetro | Tipo | Default | Descripción |
|---|---|---|---|
| `page` | int | `0` | Número de página (0-indexed) |
| `size` | int | `20` | Tamaño de página (max 100) |
| `estado` | EstadoPaquete | — | Filtrar por estado (opcional) |
| `fechaDesde` | date (ISO-8601) | — | Filtrar desde fecha de ingreso (opcional) |
| `fechaHasta` | date (ISO-8601) | — | Filtrar hasta fecha de ingreso (opcional) |

**Response 200 OK:**
```json
{
  "content": [
    {
      "id": "uuid",
      "etiquetaDigital": "PK-UUID",
      "estado": "RECIBIDO_EN_SEDE",
      "tipoMercancia": "ESTANDAR",
      "remitente": "Carlos López",
      "destinatario": "María García",
      "ciudadDestino": "Medellín",
      "fechaIngresoUtc": "2026-05-12T05:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "last": false,
  "first": true
}
```

---

#### `POST /api/paquetes/admision`
Registrar la admisión de un paquete.

**Request Body:**
```json
{
  "remitente": {
    "tipoDocumento": "CEDULA_CIUDADANIA",
    "numeroDocumento": "123456789",
    "nombreCompleto": "Carlos López",
    "telefono": "3001234567",
    "correoElectronico": "carlos@email.com",
    "direccion": {
      "direccion": "Calle 123 #45-67",
      "ciudad": "Bogotá",
      "departamento": "Cundinamarca",
      "pais": "Colombia"
    }
  },
  "destinatario": {
    "tipoDocumento": "CEDULA_CIUDADANIA",
    "numeroDocumento": "987654321",
    "nombreCompleto": "María García",
    "telefono": "3007654321",
    "correoElectronico": "maria@email.com",
    "direccion": {
      "direccion": "Carrera 50 #20-10",
      "ciudad": "Medellín",
      "departamento": "Antioquia",
      "pais": "Colombia"
    }
  },
  "direccionDestino": {
    "direccion": "Carrera 50 #20-10",
    "ciudad": "Medellín",
    "departamento": "Antioquia",
    "pais": "Colombia"
  },
  "peso": 5.0,
  "largo": 30.0,
  "ancho": 20.0,
  "alto": 15.0,
  "tipoMercancia": "ESTANDAR",
  "valorDeclarado": 500000.0,
  "metodoPago": "PREPAGO",
  "sedeId": "550e8400-e29b-41d4-a716-446655440001"
}
```

**Response 200 OK:**
```json
{
  "paqueteId": "uuid-del-paquete",
  "etiquetaDigital": "PK-uuid",
  "estado": "RECIBIDO_EN_SEDE",
  "pesoFacturable": 6.0,
  "categoriaCarga": "NORMAL",
  "precioEnvio": 15000.0,
  "distanciaEstimadaKm": 415.0,
  "fechaIngresoUtc": "2026-05-12T05:00:00Z"
}
```

---

#### `GET /api/paquetes/{idPaquete}`
Consultar un paquete por ID.

**Path Parameter:** `idPaquete` (UUID)

**Response 200 OK:**
```json
{
  "id": "uuid",
  "etiquetaDigital": "PK-uuid",
  "estado": "RECIBIDO_EN_SEDE",
  "remitente": { ... },
  "destinatario": { ... },
  "direccionDestino": { ... },
  "peso": { ... },
  "dimensiones": { ... },
  "tipoMercancia": "ESTANDAR",
  "valorDeclarado": 500000.0,
  "metodoPago": "PREPAGO",
  "precioEnvio": 15000.0,
  "fechaIngresoUtc": "2026-05-12T05:00:00Z"
}
```

**Response 404 Not Found:**
```json
{
  "codigo": "PAQUETE_NO_ENCONTRADO",
  "mensaje": "Paquete no encontrado con ID: uuid",
  "status": 404
}
```

---

#### `POST /api/paquetes/pesaje`
Procesar pesaje de un paquete.

**Request Body:**
```json
{
  "paqueteId": "uuid",
  "pesoKg": 5.0,
  "largoCm": 30.0,
  "anchoCm": 20.0,
  "altoCm": 15.0,
  "indicadorFormaIrregular": false
}
```

**Response 200 OK:**
```json
{
  "paqueteId": "uuid",
  "pesoFacturable": 6.0,
  "categoriaCarga": "NORMAL",
  "precioEnvio": 15000.0,
  "distanciaEstimadaKm": 415.0,
  "volumenM3": 0.009,
  "pesoVolumetrico": 1.5,
  "alertaDensidadAtipica": false,
  "alertaCargaEspecial": false
}
```

---

#### `GET /api/paquetes/pesaje/health`
Health check del servicio de pesaje.

**Response 200 OK:**
```text
OK
```

---

#### `GET /api/paquetes/{paqueteId}/almacenaje/sugerencia`
Obtener sugerencia de zona de almacenaje.

**Response 200 OK:**
```json
{
  "zonaId": "uuid",
  "nombre": "Zona Normal A1",
  "codigo": "ZA-NORMAL-A1",
  "categoria": "NORMAL",
  "ubicacionFisica": "Pasillo A, Estanteria 1"
}
```

---

#### `POST /api/paquetes/{paqueteId}/almacenaje`
Asignar paquete a una zona de almacenaje.

**Request Body:**
```json
{
  "zonaId": "uuid"
}
```

**Response 200 OK:**
```json
{
  "paqueteId": "uuid",
  "zonaAsignada": "Zona Normal A1",
  "estado": "EN_CLASIFICACION",
  "mensaje": "Paquete asignado a zona de almacenaje exitosamente"
}
```

---

#### `GET /api/paquetes/clasificacion/sugerencia/{paqueteId}`
Sugerencia de zona destino para clasificación.

**Response 200 OK:**
```json
{
  "paqueteId": "uuid",
  "sugerencias": [
    {
      "zonaDestinoId": "uuid",
      "nombre": "Zona Norte Medellín",
      "codigo": "ZD-MED-NORTE",
      "categoria": "NORMAL",
      "compatible": true
    }
  ]
}
```

---

#### `POST /api/paquetes/clasificacion/confirmar`
Confirmar clasificación de paquete.

**Request Body:**
```json
{
  "paqueteId": "uuid",
  "zonaDestinoId": "uuid"
}
```

**Response 200 OK:**
```json
{
  "paqueteId": "uuid",
  "zonaDestino": "Zona Norte Medellín",
  "estado": "LISTO_PARA_DESPACHO",
  "mensaje": "Clasificación confirmada exitosamente"
}
```

---

#### `POST /api/paquetes/{paqueteId}/novedades`
Reportar una novedad sobre un paquete (multipart).

> **Precisión técnica:** El backend recibe los campos de texto como parámetros individuales del formulario (`@ModelAttribute`), **no** como un JSON dentro de `@RequestPart`. El archivo de evidencia se envía como un campo `MultipartFile` independiente en el mismo `multipart/form-data`.

**Request:** `multipart/form-data`

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `tipoNovedad` | String | Sí | `DAÑADO` o `EXTRAVIADO` |
| `observaciones` | String | No | Descripción de la novedad |
| `usuarioId` | UUID | Sí | ID del usuario que reporta |
| `evidencia` | File | No | Archivo de evidencia (imagen, PDF). Obligatorio si `tipoNovedad=DAÑADO` |

**Ejemplo con cURL:**
```bash
curl -X POST http://localhost:8080/api/paquetes/{paqueteId}/novedades \
  -H "Authorization: Bearer <token>" \
  -F "tipoNovedad=DAÑADO" \
  -F "observaciones=Paquete con abolladura" \
  -F "usuarioId=550e8400-e29b-41d4-a716-446655440002" \
  -F "evidencia=@foto.jpg"
```

**Response 201 Created:**
```json
{
  "paqueteId": "uuid",
  "estadoActual": "NOVEDAD_EN_BODEGA",
  "historialId": "uuid",
  "mensaje": "Novedad registrada exitosamente"
}
```

---

### 3. Consultas Financieras (Requiere JWT)

#### `GET /route/{idRoute}/package/{idPaquete}`
Consultar estado y ruta de un paquete para el módulo financiero.

**Response 200 OK:**
```json
{
  "paqueteId": "uuid",
  "rutaId": "uuid",
  "estadoActual": "EN_TRANSITO",
  "historialEstados": [
    {
      "estado": "RECIBIDO_EN_SEDE",
      "fecha": "2026-05-12T05:00:00Z",
      "observaciones": "Paquete recibido en sede"
    }
  ],
  "fechaIngreso": "2026-05-12T05:00:00Z",
  "fechaEntrega": null
}
```

---

## Esquemas de Datos (Value Objects)

### `Direccion`
```json
{
  "direccion": "string",
  "ciudad": "string",
  "departamento": "string",
  "pais": "string"
}
```

### `Persona` (Remitente / Destinatario)
```json
{
  "tipoDocumento": "CEDULA_CIUDADANIA | CEDULA_EXTRANJERIA | PASAPORTE | NIT",
  "numeroDocumento": "string",
  "nombreCompleto": "string",
  "telefono": "string",
  "correoElectronico": "string",
  "direccion": "Direccion"
}
```

### Enumeraciones Comunes

| Enum | Valores |
|---|---|
| `tipoDocumento` | `CEDULA_CIUDADANIA`, `CEDULA_EXTRANJERIA`, `PASAPORTE`, `NIT` |
| `tipoMercancia` | `ESTANDAR`, `FRAGIL`, `PELIGROSO` |
| `metodoPago` | `PREPAGO`, `CONTRA_ENTREGA` |
| `categoriaCarga` | `NORMAL`, `CARGA_ESPECIAL` |
| `estadoPaquete` | `RECIBIDO_EN_SEDE`, `EN_CLASIFICACION`, `LISTO_PARA_DESPACHO`, `EN_TRANSITO`, `ENTREGADO`, `NOVEDAD_EN_BODEGA`, `EN_PARADA_DE_ENTREGA`, `DEVOLUCION_EN_RUTA`, `EXTRAVIADO_EN_RUTA`, `DAÑADO_EN_RUTA` |
| `tipoNovedad` | `DAÑADO`, `EXTRAVIADO` |

---

## Formato de Errores (General)

Todas las respuestas de error se manejan a través de un `@RestControllerAdvice` global y siguen esta estructura estandarizada:

```json
{
  "timestamp": "2026-05-12T05:00:00Z",
  "status": 400,
  "codigo": "CODIGO_ERROR",
  "mensaje": "Descripción del error para el usuario",
  "errores": null
}
```

Para errores de validación de campos, se incluye el arreglo `errores`:

```json
{
  "timestamp": "2026-05-12T05:00:00Z",
  "status": 400,
  "codigo": "ERROR_VALIDACION",
  "mensaje": "Errores de validación en los datos de entrada",
  "errores": [
    { "campo": "peso", "mensaje": "El peso debe ser positivo" },
    { "campo": "tipoMercancia", "mensaje": "El tipo de mercancía es obligatorio" }
  ]
}
```

| Código de Error | HTTP Status | Causa |
|---|---|---|
| `PAQUETE_NO_ENCONTRADO` | 404 | ID de paquete no existe |
| `TRANSICION_INVALIDA` | 400 | Cambio de estado no permitido |
| `EVIDENCIA_REQUERIDA` | 400 | Falta archivo de evidencia |
| `ERROR_VALIDACION` | 400 | Errores de validación en los campos |
| `ARGUMENTO_INVALIDO` | 400 | Argumento inválido en la petición |
| `ZONA_NO_APTA` | 400 | Zona no apta para el paquete |
| `COORDENADAS_INVALIDAS` | 400 | Coordenadas geográficas inválidas |
| `COBERTURA_INVALIDA` | 400 | Cobertura geográfica inválida |
| `CREDENCIALES_INVALIDAS` | 401 | Usuario o contraseña incorrectos |
| `RECURSO_NO_ENCONTRADO` | 404 | Endpoint o recurso no existe |
| `ZONA_ALMACENAJE_NO_ENCONTRADA` | 404 | Zona de almacenaje no existe |
| `ZONA_DESTINO_NO_ENCONTRADA` | 404 | Zona de destino no existe |
| `CONFLICTO_CONCURRENCIA` | 409 | Optimistic lock (reintentar) |
| `ZONA_SATURADA` | 409 | Zona sin capacidad disponible |
| `ESTADO_INVALIDO` | 409 | Estado de paquete no permite la operación |
| `EVENTO_DUPLICADO` | 409 | Evento duplicado |
| `ERROR_INTERNO` | 500 | Error inesperado del servidor |

---

## Notas Operativas para Agentes de Integración

- **Validación de UUIDs:** Antes de enviar un ID al backend, valida formato UUID v4. Peticiones con UUIDs inválidos serán rechazadas con `400 ARGUMENTO_INVALIDO`.
- **Zona de Contingencia:** Si una zona de almacenamiento está saturada, la API retorna `zonaContingenciaId` en la respuesta. Redirige automáticamente a la zona alternativa.
- **Límite de peso:** El peso real del paquete no debe exceder 70.0 kg. Pesos superiores causan `400 ARGUMENTO_INVALIDO`.
- **Novedad DAÑADO:** Es obligatorio adjuntar archivo de evidencia (imagen/PDF). Sin evidencia → `400 EVIDENCIA_REQUERIDA`.
- **UTC:** Todas las fechas se procesan estrictamente en UTC (ISO-8601 con sufijo `Z`). El frontend debe convertir a local solo para visualización.

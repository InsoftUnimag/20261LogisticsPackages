# API Externa — Consulta de Sedes Operativas

## Definición del Endpoint

| Propiedad | Valor |
|---|---|
| **URL Producción** | `https://api.corporativo.com/v1/sedes` |
| **URL Desarrollo** | `http://localhost:8000/api/v1/sedes` |
| **Método HTTP** | `GET` |
| **Autenticación** | `Bearer Token` (JWT) |
| **Content-Type** | `application/json` |

---

## Estructura de Solicitud (Request)

### Path Variables

| Variable | Tipo | Requerido | Descripción |
|---|---|---|---|
| `{idSede}` | `UUID` | No | UUID de la sede específica. Si se omite, retorna listado paginado. |

### Query Parameters

| Parámetro | Tipo | Requerido | Descripción |
|---|---|---|---|
| `page` | `Integer` | Opcional | Número de página (default: `0`) |
| `size` | `Integer` | Opcional | Elementos por página (default: `20`, max: `100`) |
| `sort` | `String` | Opcional | Campo de ordenación (ej. `nombre,asc`) |
| `tipo` | `String` | Opcional | Filtro por tipo de sede: `PRINCIPAL`, `AUXILIAR` |
| `ciudad` | `String` | Opcional | Filtro por ciudad |
| `activo` | `Boolean` | Opcional | Filtro por estado activo/inactivo |

### Ejemplo de Llamada

```http
GET /api/v1/sedes?page=0&size=20&tipo=PRINCIPAL&ciudad=Bogot%C3%A1
Host: api.corporativo.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

```http
GET /api/v1/sedes/550e8400-e29b-41d4-a716-446655440001
Host: api.corporativo.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## Estructura de Respuesta (Response JSON)

### Listado Paginado — `200 OK`

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "nombre": "Sede Central Bogotá",
      "codigo": "SC-BOG-001",
      "tipo": "PRINCIPAL",
      "direccion": {
        "direccion": "Cra 7 # 32-10",
        "ciudad": "Bogotá",
        "departamento": "Cundinamarca",
        "pais": "Colombia"
      },
      "coordenadas": {
        "latitud": 4.624335,
        "longitud": -74.063644
      },
      "capacidadMaximaPeso": 50000.00,
      "capacidadMaximaVolumen": 2500.00,
      "tarifaBase": 8000.00,
      "tarifaPorKg": 1200.00,
      "tarifaPorKm": 500.00,
      "metodosPagoHabilitados": ["PREPAGO", "CONTRA_ENTREGA"],
      "activo": true,
      "fechaCreacion": "2025-01-15T08:00:00Z",
      "fechaActualizacion": "2026-05-16T14:19:20Z"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "nombre": "Sede Medellín",
      "codigo": "SC-MED-002",
      "tipo": "AUXILIAR",
      "direccion": {
        "direccion": "Cll 50 # 45-20",
        "ciudad": "Medellín",
        "departamento": "Antioquia",
        "pais": "Colombia"
      },
      "coordenadas": {
        "latitud": 6.247638,
        "longitud": -75.565815
      },
      "capacidadMaximaPeso": 30000.00,
      "capacidadMaximaVolumen": 1500.00,
      "tarifaBase": 7500.00,
      "tarifaPorKg": 1100.00,
      "tarifaPorKm": 480.00,
      "metodosPagoHabilitados": ["PREPAGO", "CONTRA_ENTREGA"],
      "activo": true,
      "fechaCreacion": "2025-03-20T10:30:00Z",
      "fechaActualizacion": "2026-04-10T09:15:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 2,
  "totalPages": 1,
  "last": true
}
```

### Recurso Individual — `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "nombre": "Sede Central Bogotá",
  "codigo": "SC-BOG-001",
  "tipo": "PRINCIPAL",
  "direccion": {
    "direccion": "Cra 7 # 32-10",
    "ciudad": "Bogotá",
    "departamento": "Cundinamarca",
    "pais": "Colombia"
  },
  "coordenadas": {
    "latitud": 4.624335,
    "longitud": -74.063644
  },
  "capacidadMaximaPeso": 50000.00,
  "capacidadMaximaVolumen": 2500.00,
  "tarifaBase": 8000.00,
  "tarifaPorKg": 1200.00,
  "tarifaPorKm": 500.00,
  "metodosPagoHabilitados": ["PREPAGO", "CONTRA_ENTREGA"],
  "activo": true,
  "fechaCreacion": "2025-01-15T08:00:00Z",
  "fechaActualizacion": "2026-05-16T14:19:20Z"
}
```

---

## Diccionario de Datos

### Campos del Objeto `Sede`

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id` | `UUID` | Requerido, formato UUID v4 | Identificador único de la sede |
| `nombre` | `String` | Requerido, max 150 chars | Nombre comercial de la sede |
| `codigo` | `String` | Requerido, único, max 20 chars | Código interno corporativo (ej. `SC-BOG-001`) |
| `tipo` | `String` | Requerido, enum | Tipo de sede: `PRINCIPAL` (matriz) o `AUXILIAR` (sucursal) |
| `direccion` | `Object` | Requerido | Objeto anidado `Direccion` (ver tabla abajo) |
| `coordenadas` | `Object` | Requerido | Objeto anidado con `latitud` y `longitud` (WGS-84) |
| `coordenadas.latitud` | `Double` | Requerido, rango `[-90.0, 90.0]` | Latitud geográfica en grados decimales |
| `coordenadas.longitud` | `Double` | Requerido, rango `[-180.0, 180.0]` | Longitud geográfica en grados decimales |
| `capacidadMaximaPeso` | `BigDecimal` | Requerido, >= 0, 2 decimales | Capacidad máxima de peso en kilogramos |
| `capacidadMaximaVolumen` | `BigDecimal` | Requerido, >= 0, 2 decimales | Capacidad máxima de volumen en metros cúbicos |
| `tarifaBase` | `BigDecimal` | Requerido, >= 0, 2 decimales | Tarifa base de envío en COP |
| `tarifaPorKg` | `BigDecimal` | Requerido, >= 0, 2 decimales | Tarifa por kilogramo adicional en COP |
| `tarifaPorKm` | `BigDecimal` | Requerido, >= 0, 2 decimales | Tarifa por kilómetro de distancia en COP |
| `metodosPagoHabilitados` | `Array<String>` | Requerido, min 1 elemento | Métodos de pago disponibles: `PREPAGO`, `CONTRA_ENTREGA` |
| `activo` | `Boolean` | Requerido | Indica si la sede está operativa |
| `fechaCreacion` | `String` | Requerido, ISO-8601 UTC con sufijo `Z` | Marca temporal de creación del registro |
| `fechaActualizacion` | `String` | Opcional, ISO-8601 UTC con sufijo `Z` | Marca temporal de la última actualización |

### Campos del Objeto `Direccion`

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `direccion` | `String` | Requerido, max 255 chars | Dirección física (calle, carrera, número) |
| `ciudad` | `String` | Requerido, max 100 chars | Ciudad de ubicación |
| `departamento` | `String` | Requerido, max 100 chars | Departamento o estado |
| `pais` | `String` | Requerido, max 100 chars | País de ubicación |

---

## Manejo de Errores y Casos de Borde

### `400 Bad Request` — Parámetros de consulta inválidos

```json
{
  "timestamp": "2026-05-16T14:19:20Z",
  "status": 400,
  "codigo": "SOLICITUD_INVALIDA",
  "mensaje": "Los parámetros de consulta proporcionados no son válidos.",
  "errores": [
    {
      "campo": "page",
      "mensaje": "Debe ser un número entero mayor o igual a 0"
    },
    {
      "campo": "tipo",
      "mensaje": "El valor 'SUCURSAL' no es válido. Valores permitidos: PRINCIPAL, AUXILIAR"
    }
  ],
  "path": "/api/v1/sedes"
}
```

### `401 Unauthorized` — Token ausente o inválido

```json
{
  "timestamp": "2026-05-16T14:19:20Z",
  "status": 401,
  "codigo": "NO_AUTENTICADO",
  "mensaje": "Token de autenticación ausente, inválido o expirado.",
  "errores": null,
  "path": "/api/v1/sedes"
}
```

### `404 Not Found` — Sede no encontrada por UUID

```json
{
  "timestamp": "2026-05-16T14:19:20Z",
  "status": 404,
  "codigo": "SEDE_NO_ENCONTRADA",
  "mensaje": "No se encontró una sede con el ID 550e8400-e29b-41d4-a716-446655449999.",
  "errores": null,
  "path": "/api/v1/sedes/550e8400-e29b-41d4-a716-446655449999"
}
```

### `500 Internal Server Error` — Error inesperado

```json
{
  "timestamp": "2026-05-16T14:19:20Z",
  "status": 500,
  "codigo": "ERROR_INTERNO",
  "mensaje": "Ha ocurrido un error inesperado. Intente nuevamente más tarde.",
  "errores": null,
  "path": "/api/v1/sedes"
}
```

---

## Notas Adicionales

- Las coordenadas geográficas utilizan el sistema de referencia **WGS-84** (EPSG:4326).
- Las tarifas están expresadas en **pesos colombianos (COP)**.
- Los resultados paginados siguen el formato **Spring Data REST** (`content`, `page`, `size`, `totalElements`, `totalPages`).
- Los sellos de tiempo (`timestamp`, `fechaCreacion`, `fechaActualizacion`) se expresan exclusivamente en **UTC (ISO-8601)** con sufijo `Z`.

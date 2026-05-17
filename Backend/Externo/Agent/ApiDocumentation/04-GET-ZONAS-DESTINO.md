# API Externa — Consulta de Zonas de Destino

## Definición del Endpoint

| Propiedad | Valor |
|---|---|
| **URL Producción** | `https://api.corporativo.com/v1/zonas-destino` |
| **URL Desarrollo** | `http://localhost:8000/api/v1/zonas-destino` |
| **Método HTTP** | `GET` |
| **Autenticación** | `Bearer Token` (JWT) |
| **Content-Type** | `application/json` |

---

## Estructura de Solicitud (Request)

### Path Variables

| Variable | Tipo | Requerido | Descripción |
|---|---|---|---|
| `{idZona}` | `UUID` | No | UUID de la zona de destino específica. Si se omite, retorna listado paginado. |

### Query Parameters

| Parámetro | Tipo | Requerido | Descripción |
|---|---|---|---|
| `page` | `Integer` | Opcional | Número de página (default: `0`) |
| `size` | `Integer` | Opcional | Elementos por página (default: `20`, max: `100`) |
| `sort` | `String` | Opcional | Campo de ordenación (ej. `nombre,asc`) |
| `idSede` | `UUID` | **Requerido** | UUID de la sede desde la cual se despachan los paquetes |
| `categoria` | `String` | Opcional | Filtro por categoría: `NORMAL`, `DELICADA`, `ALTO_RIESGO`, `RETENCION` |
| `codigo` | `String` | Opcional | Búsqueda por código exacto (ej. `ZD-NORTE-01`) |
| `latitud` | `Double` | Opcional | Latitud del punto de destino para búsqueda geográfica (requiere `longitud`) |
| `longitud` | `Double` | Opcional | Longitud del punto de destino para búsqueda geográfica (requiere `latitud`) |

### Ejemplo de Llamada

```http
GET /api/v1/zonas-destino?idSede=550e8400-e29b-41d4-a716-446655440001&categoria=NORMAL
Host: api.corporativo.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

```http
GET /api/v1/zonas-destino/660e8400-e29b-41d4-a716-446655440001
Host: api.corporativo.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

```http
GET /api/v1/zonas-destino?idSede=550e8400-e29b-41d4-a716-446655440001&latitud=4.7110&longitud=-74.0721
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
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "nombre": "Zona Destino Norte",
      "codigo": "ZD-NORTE-01",
      "categoria": "NORMAL",
      "latitudMin": 4.7500,
      "latitudMax": 4.8000,
      "longitudMin": -74.1000,
      "longitudMax": -74.0200,
      "centroide": {
        "latitud": 4.7750,
        "longitud": -74.0600
      },
      "capacidadMaxPaquetes": 300,
      "contadorPaquetes": 42,
      "disponible": true,
      "idSede": "550e8400-e29b-41d4-a716-446655440001",
      "fechaCreacion": "2025-01-15T08:00:00Z",
      "fechaActualizacion": "2026-05-16T14:19:20Z"
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440002",
      "nombre": "Zona Destino Sur",
      "codigo": "ZD-SUR-01",
      "categoria": "NORMAL",
      "latitudMin": 4.5000,
      "latitudMax": 4.6000,
      "longitudMin": -74.1800,
      "longitudMax": -74.0800,
      "centroide": {
        "latitud": 4.5500,
        "longitud": -74.1300
      },
      "capacidadMaxPaquetes": 300,
      "contadorPaquetes": 218,
      "disponible": true,
      "idSede": "550e8400-e29b-41d4-a716-446655440001",
      "fechaCreacion": "2025-01-15T08:00:00Z",
      "fechaActualizacion": "2026-05-15T18:00:00Z"
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440003",
      "nombre": "Zona Destino Centro",
      "codigo": "ZD-CENTRO-01",
      "categoria": "DELICADA",
      "latitudMin": 4.6000,
      "latitudMax": 4.7500,
      "longitudMin": -74.1200,
      "longitudMax": -74.0500,
      "centroide": {
        "latitud": 4.6750,
        "longitud": -74.0850
      },
      "capacidadMaxPaquetes": 150,
      "contadorPaquetes": 150,
      "disponible": false,
      "idSede": "550e8400-e29b-41d4-a716-446655440001",
      "fechaCreacion": "2025-02-01T10:00:00Z",
      "fechaActualizacion": "2026-05-16T08:00:00Z"
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440004",
      "nombre": "Zona Destino Fragil Especial",
      "codigo": "ZD-FRAGIL-ESP-01",
      "categoria": "DELICADA",
      "latitudMin": 4.7000,
      "latitudMax": 4.7800,
      "longitudMin": -74.0800,
      "longitudMax": -74.0100,
      "centroide": {
        "latitud": 4.7400,
        "longitud": -74.0450
      },
      "capacidadMaxPaquetes": 100,
      "contadorPaquetes": 32,
      "disponible": true,
      "idSede": "550e8400-e29b-41d4-a716-446655440001",
      "fechaCreacion": "2025-04-10T09:00:00Z",
      "fechaActualizacion": "2026-05-14T16:30:00Z"
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440005",
      "nombre": "Zona Destino Peligroso",
      "codigo": "ZD-PELIGROSO-01",
      "categoria": "ALTO_RIESGO",
      "latitudMin": 4.5500,
      "latitudMax": 4.6500,
      "longitudMin": -74.1600,
      "longitudMax": -74.0600,
      "centroide": {
        "latitud": 4.6000,
        "longitud": -74.1100
      },
      "capacidadMaxPaquetes": 50,
      "contadorPaquetes": 12,
      "disponible": true,
      "idSede": "550e8400-e29b-41d4-a716-446655440001",
      "fechaCreacion": "2025-06-01T08:00:00Z",
      "fechaActualizacion": "2026-05-10T11:00:00Z"
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440006",
      "nombre": "Zona Destino Retencion",
      "codigo": "ZD-RETENCION-01",
      "categoria": "RETENCION",
      "latitudMin": 4.5000,
      "latitudMax": 4.8000,
      "longitudMin": -74.1800,
      "longitudMax": -74.0100,
      "centroide": {
        "latitud": 4.6500,
        "longitud": -74.0950
      },
      "capacidadMaxPaquetes": 1000,
      "contadorPaquetes": 0,
      "disponible": true,
      "idSede": "550e8400-e29b-41d4-a716-446655440001",
      "fechaCreacion": "2025-01-15T08:00:00Z",
      "fechaActualizacion": "2026-01-01T00:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 6,
  "totalPages": 1,
  "last": true
}
```

### Recurso Individual — `200 OK`

```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "nombre": "Zona Destino Norte",
  "codigo": "ZD-NORTE-01",
  "categoria": "NORMAL",
  "latitudMin": 4.7500,
  "latitudMax": 4.8000,
  "longitudMin": -74.1000,
  "longitudMax": -74.0200,
  "centroide": {
    "latitud": 4.7750,
    "longitud": -74.0600
  },
  "capacidadMaxPaquetes": 300,
  "contadorPaquetes": 42,
  "disponible": true,
  "idSede": "550e8400-e29b-41d4-a716-446655440001",
  "fechaCreacion": "2025-01-15T08:00:00Z",
  "fechaActualizacion": "2026-05-16T14:19:20Z"
}
```

---

## Diccionario de Datos

### Campos del Objeto `ZonaDestino`

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id` | `UUID` | Requerido, formato UUID v4 | Identificador único de la zona de destino |
| `nombre` | `String` | Requerido, max 150 chars | Nombre descriptivo de la zona geográfica |
| `codigo` | `String` | Requerido, único, max 20 chars | Código interno normalizado (ej. `ZD-NORTE-01`, `ZD-SUR-01`, `ZD-FRAGIL-ESP-01`) |
| `categoria` | `String` | Requerido, enum | Categoría de la zona: `NORMAL`, `DELICADA`, `ALTO_RIESGO`, `RETENCION`. Determina compatibilidad con tipos de mercancía (misma matriz que Zonas de Almacenaje). |
| `latitudMin` | `Double` | Requerido, rango `[-90.0, 90.0]` | Límite sur del bounding box geográfico (WGS-84) |
| `latitudMax` | `Double` | Requerido, rango `[-90.0, 90.0]` | Límite norte del bounding box geográfico (WGS-84) |
| `longitudMin` | `Double` | Requerido, rango `[-180.0, 180.0]` | Límite oeste del bounding box geográfico (WGS-84) |
| `longitudMax` | `Double` | Requerido, rango `[-180.0, 180.0]` | Límite este del bounding box geográfico (WGS-84) |
| `centroide` | `Object` | Requerido | Punto geográfico central del área de cobertura. **Campo derivado (solo lectura).** |
| `centroide.latitud` | `Double` | Requerido, `= (latitudMin + latitudMax) / 2` | Latitud del centroide |
| `centroide.longitud` | `Double` | Requerido, `= (longitudMin + longitudMax) / 2` | Longitud del centroide |
| `capacidadMaxPaquetes` | `Integer` | Requerido, >= 1 | Capacidad máxima de paquetes en ruta hacia esta zona |
| `contadorPaquetes` | `Integer` | Requerido, >= 0 | Paquetes actualmente asignados a esta zona de destino |
| `disponible` | `Boolean` | Requerido | `true` si `contadorPaquetes < capacidadMaxPaquetes`. **Campo derivado (solo lectura).** |
| `idSede` | `UUID` | Requerido, formato UUID v4 | Identificador de la sede origen desde la que se gestionan los despachos hacia esta zona |
| `fechaCreacion` | `String` | Requerido, ISO-8601 UTC con sufijo `Z` | Marca temporal de creación del registro |
| `fechaActualizacion` | `String` | Opcional, ISO-8601 UTC con sufijo `Z` | Marca temporal de la última actualización |

### Campos del Objeto `Centroide`

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `latitud` | `Double` | Requerido, rango `[-90.0, 90.0]` | Latitud central del bounding box (WGS-84) |
| `longitud` | `Double` | Requerido, rango `[-180.0, 180.0]` | Longitud central del bounding box (WGS-84) |

### Validaciones Geográficas (Bounding Box)

- El bounding box se define mediante coordenadas **WGS-84** (EPSG:4326).
- `latitudMin` debe ser **estrictamente menor** que `latitudMax`.
- `longitudMin` debe ser **estrictamente menor** que `longitudMax`.
- La pertinencia geográfica de un paquete se determina mediante: `latitudMin <= paquete.latitud <= latitudMax AND longitudMin <= paquete.longitud <= longitudMax`.
- Los rangos de latitud/longitud no deben exceder 1 grado en ninguna dimensión para mantener zonas de destino granularmente definidas.

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
      "campo": "idSede",
      "mensaje": "El parámetro 'idSede' es requerido para consultar zonas de destino"
    },
    {
      "campo": "latitud",
      "mensaje": "Si se proporciona 'latitud', el parámetro 'longitud' también es requerido"
    },
    {
      "campo": "latitud",
      "mensaje": "El valor 100.0 está fuera del rango válido [-90.0, 90.0]"
    }
  ],
  "path": "/api/v1/zonas-destino"
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
  "path": "/api/v1/zonas-destino"
}
```

### `404 Not Found` — Zona de destino no encontrada

```json
{
  "timestamp": "2026-05-16T14:19:20Z",
  "status": 404,
  "codigo": "ZONA_DESTINO_NO_ENCONTRADA",
  "mensaje": "No se encontró una zona de destino con el ID 660e8400-e29b-41d4-a716-446655449999.",
  "errores": null,
  "path": "/api/v1/zonas-destino/660e8400-e29b-41d4-a716-446655449999"
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
  "path": "/api/v1/zonas-destino"
}
```

---

## Notas Adicionales

- El `idSede` es un filtro **obligatorio**; las zonas de destino se organizan por sede de origen.
- La determinación geográfica se realiza mediante **Bounding Box** (caja delimitadora). El punto de coordenadas del paquete debe caer dentro del rectángulo definido por `[latitudMin, longitudMin]` a `[latitudMax, longitudMax]`.
- El campo `disponible` es **derivado** (`contadorPaquetes < capacidadMaxPaquetes`). Si `disponible: false`, el endpoint de clasificación debe rechazar asignaciones adicionales.
- Los parámetros `latitud` y `longitud` en la consulta permiten búsqueda geográfica inversa: el servidor retorna únicamente las zonas cuyo bounding box contiene el punto consultado.
- El `centroide` es un campo calculado que representa el punto central del área de cobertura para visualización en mapas.
- Los sellos de tiempo (`timestamp`, `fechaCreacion`, `fechaActualizacion`) se expresan exclusivamente en **UTC (ISO-8601)** con sufijo `Z`.

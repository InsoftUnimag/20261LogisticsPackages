# API Externa — Consulta de Zonas de Almacenaje

## Definición del Endpoint

| Propiedad | Valor |
|---|---|
| **URL Producción** | `https://api.corporativo.com/v1/zonas-almacenaje` |
| **URL Desarrollo** | `http://localhost:8000/api/v1/zonas-almacenaje` |
| **Método HTTP** | `GET` |
| **Autenticación** | `Bearer Token` (JWT) |
| **Content-Type** | `application/json` |

---

## Estructura de Solicitud (Request)

### Path Variables

| Variable | Tipo | Requerido | Descripción |
|---|---|---|---|
| `{idZona}` | `UUID` | No | UUID de la zona de almacenaje específica. Si se omite, retorna listado paginado. |

### Query Parameters

| Parámetro | Tipo | Requerido | Descripción |
|---|---|---|---|
| `page` | `Integer` | Opcional | Número de página (default: `0`) |
| `size` | `Integer` | Opcional | Elementos por página (default: `20`, max: `100`) |
| `sort` | `String` | Opcional | Campo de ordenación (ej. `nombre,asc`) |
| `idSede` | `UUID` | **Requerido** | UUID de la sede para filtrar zonas pertenecientes |
| `categoria` | `String` | Opcional | Filtro por categoría: `NORMAL`, `DELICADA`, `ALTO_RIESGO`, `RETENCION` |
| `estado` | `String` | Opcional | Filtro por estado: `DISPONIBLE`, `PARCIAL`, `SATURADO`, `BLOQUEADO` |
| `codigo` | `String` | Opcional | Búsqueda por código exacto (ej. `ZA-NORMAL-A1`) |

### Ejemplo de Llamada

```http
GET /api/v1/zonas-almacenaje?idSede=550e8400-e29b-41d4-a716-446655440001&categoria=NORMAL&estado=DISPONIBLE
Host: api.corporativo.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

```http
GET /api/v1/zonas-almacenaje/770e8400-e29b-41d4-a716-446655440001
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
      "id": "770e8400-e29b-41d4-a716-446655440001",
      "nombre": "Zona Normal A1",
      "codigo": "ZA-NORMAL-A1",
      "categoria": "NORMAL",
      "capacidadMaxKg": 10000.00,
      "capacidadMaxM3": 500.00,
      "capacidadMaxPaquetes": 500,
      "pesoActualKg": 3250.50,
      "volumenActualM3": 180.25,
      "contadorPaquetes": 143,
      "porcentajeOcupacion": 32.51,
      "estado": "DISPONIBLE",
      "ubicacionFisica": "Bodega 1, Estantería A, Nivel 1-3",
      "idSede": "550e8400-e29b-41d4-a716-446655440001",
      "zonaContingenciaId": "770e8400-e29b-41d4-a716-446655440002",
      "fechaCreacion": "2025-01-15T08:00:00Z",
      "fechaActualizacion": "2026-05-16T14:19:20Z"
    },
    {
      "id": "770e8400-e29b-41d4-a716-446655440002",
      "nombre": "Zona Normal A2",
      "codigo": "ZA-NORMAL-A2",
      "categoria": "NORMAL",
      "capacidadMaxKg": 10000.00,
      "capacidadMaxM3": 500.00,
      "capacidadMaxPaquetes": 500,
      "pesoActualKg": 7800.00,
      "volumenActualM3": 420.00,
      "contadorPaquetes": 412,
      "porcentajeOcupacion": 78.00,
      "estado": "PARCIAL",
      "ubicacionFisica": "Bodega 1, Estantería A, Nivel 4-6",
      "idSede": "550e8400-e29b-41d4-a716-446655440001",
      "zonaContingenciaId": null,
      "fechaCreacion": "2025-01-15T08:00:00Z",
      "fechaActualizacion": "2026-05-16T12:00:00Z"
    },
    {
      "id": "770e8400-e29b-41d4-a716-446655440003",
      "nombre": "Zona Delicada B1",
      "codigo": "ZA-DELICADA-B1",
      "categoria": "DELICADA",
      "capacidadMaxKg": 5000.00,
      "capacidadMaxM3": 250.00,
      "capacidadMaxPaquetes": 200,
      "pesoActualKg": 4800.00,
      "volumenActualM3": 240.00,
      "contadorPaquetes": 195,
      "porcentajeOcupacion": 96.00,
      "estado": "SATURADO",
      "ubicacionFisica": "Bodega 2, Zona Climatizada, Estantería B",
      "idSede": "550e8400-e29b-41d4-a716-446655440001",
      "zonaContingenciaId": "770e8400-e29b-41d4-a716-446655440004",
      "fechaCreacion": "2025-03-01T10:00:00Z",
      "fechaActualizacion": "2026-05-16T10:30:00Z"
    },
    {
      "id": "770e8400-e29b-41d4-a716-446655440005",
      "nombre": "Zona Alto Riesgo C1",
      "codigo": "ZA-ALTO_RIESGO-C1",
      "categoria": "ALTO_RIESGO",
      "capacidadMaxKg": 2000.00,
      "capacidadMaxM3": 100.00,
      "capacidadMaxPaquetes": 50,
      "pesoActualKg": 0.00,
      "volumenActualM3": 0.00,
      "contadorPaquetes": 0,
      "porcentajeOcupacion": 0.00,
      "estado": "DISPONIBLE",
      "ubicacionFisica": "Bodega 3, Zona Aislada, Casillero C",
      "idSede": "550e8400-e29b-41d4-a716-446655440001",
      "zonaContingenciaId": null,
      "fechaCreacion": "2025-06-15T08:00:00Z",
      "fechaActualizacion": "2026-05-01T09:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 4,
  "totalPages": 1,
  "last": true
}
```

### Recurso Individual — `200 OK`

```json
{
  "id": "770e8400-e29b-41d4-a716-446655440001",
  "nombre": "Zona Normal A1",
  "codigo": "ZA-NORMAL-A1",
  "categoria": "NORMAL",
  "capacidadMaxKg": 10000.00,
  "capacidadMaxM3": 500.00,
  "capacidadMaxPaquetes": 500,
  "pesoActualKg": 3250.50,
  "volumenActualM3": 180.25,
  "contadorPaquetes": 143,
  "porcentajeOcupacion": 32.51,
  "estado": "DISPONIBLE",
  "ubicacionFisica": "Bodega 1, Estantería A, Nivel 1-3",
  "idSede": "550e8400-e29b-41d4-a716-446655440001",
  "zonaContingenciaId": "770e8400-e29b-41d4-a716-446655440002",
  "fechaCreacion": "2025-01-15T08:00:00Z",
  "fechaActualizacion": "2026-05-16T14:19:20Z"
}
```

---

## Diccionario de Datos

### Campos del Objeto `ZonaAlmacenaje`

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id` | `UUID` | Requerido, formato UUID v4 | Identificador único de la zona de almacenaje |
| `nombre` | `String` | Requerido, max 150 chars | Nombre descriptivo de la zona |
| `codigo` | `String` | Requerido, único, max 20 chars | Código interno normalizado (ej. `ZA-NORMAL-A1`, `ZA-DELICADA-B1`, `ZA-ALTO_RIESGO-C1`) |
| `categoria` | `String` | Requerido, enum | Categoría de la zona que determina compatibilidad con tipos de mercancía: `NORMAL`, `DELICADA`, `ALTO_RIESGO`, `RETENCION` |
| `capacidadMaxKg` | `BigDecimal` | Requerido, >= 0, 2 decimales | Capacidad máxima de peso en kilogramos |
| `capacidadMaxM3` | `BigDecimal` | Requerido, >= 0, 2 decimales | Capacidad máxima de volumen en metros cúbicos |
| `capacidadMaxPaquetes` | `Integer` | Requerido, >= 1 | Número máximo de paquetes que puede albergar |
| `pesoActualKg` | `BigDecimal` | Requerido, >= 0, 2 decimales | Peso actual ocupado en kilogramos |
| `volumenActualM3` | `BigDecimal` | Requerido, >= 0, 2 decimales | Volumen actual ocupado en metros cúbicos |
| `contadorPaquetes` | `Integer` | Requerido, >= 0 | Número de paquetes actualmente almacenados |
| `porcentajeOcupacion` | `BigDecimal` | Requerido, 0.00 - 100.00, 2 decimales | Porcentaje de ocupación calculado como `max(peso%, volumen%, paquetes%)`. **Campo derivado (solo lectura).** |
| `estado` | `String` | Requerido, enum | Estado operativo de la zona: `DISPONIBLE` (<70% ocupación), `PARCIAL` (70-90%), `SATURADO` (>90%), `BLOQUEADO` (mantenimiento/inaccesible) |
| `ubicacionFisica` | `String` | Opcional, max 255 chars | Descripción textual de la ubicación física dentro de la bodega |
| `idSede` | `UUID` | Requerido, formato UUID v4 | Identificador de la sede a la que pertenece esta zona |
| `zonaContingenciaId` | `UUID` | Opcional, formato UUID v4 | UUID de la zona de contingencia (zona alternativa cuando esta zona se satura). Auto-referencia al mismo recurso. |
| `fechaCreacion` | `String` | Requerido, ISO-8601 UTC con sufijo `Z` | Marca temporal de creación del registro |
| `fechaActualizacion` | `String` | Opcional, ISO-8601 UTC con sufijo `Z` | Marca temporal de la última actualización |

### Matriz de Compatibilidad: Categoría de Zona vs. Tipo de Mercancía

| Tipo Mercancía | `NORMAL` | `DELICADA` | `ALTO_RIESGO` | `RETENCION` |
|---|---|---|---|---|
| `ESTANDAR` | ✅ | ✅ | ✅ | ❌ |
| `FRAGIL` | ❌ | ✅ | ✅ | ❌ |
| `PELIGROSO` | ❌ | ❌ | ✅ | ❌ |

### Reglas de Transición de Estado (Ocupación)

| Estado | Rango de Ocupación | Descripción |
|---|---|---|
| `DISPONIBLE` | 0% — <70% | Capacidad amplia disponible para nuevos paquetes |
| `PARCIAL` | 70% — 90% | Capacidad reducida; se recomienda usar zona de contingencia |
| `SATURADO` | >90% | Sin capacidad disponible; debe rechazar nuevos paquetes |
| `BLOQUEADO` | N/A | Zona fuera de servicio por mantenimiento, inventario o contingencia |

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
      "mensaje": "El parámetro 'idSede' es requerido para consultar zonas de almacenaje"
    },
    {
      "campo": "categoria",
      "mensaje": "El valor 'COMUN' no es válido. Valores permitidos: NORMAL, DELICADA, ALTO_RIESGO, RETENCION"
    }
  ],
  "path": "/api/v1/zonas-almacenaje"
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
  "path": "/api/v1/zonas-almacenaje"
}
```

### `404 Not Found` — Zona de almacenaje no encontrada

```json
{
  "timestamp": "2026-05-16T14:19:20Z",
  "status": 404,
  "codigo": "ZONA_ALMACENAJE_NO_ENCONTRADA",
  "mensaje": "No se encontró una zona de almacenaje con el ID 770e8400-e29b-41d4-a716-446655449999.",
  "errores": null,
  "path": "/api/v1/zonas-almacenaje/770e8400-e29b-41d4-a716-446655449999"
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
  "path": "/api/v1/zonas-almacenaje"
}
```

---

## Notas Adicionales

- El `idSede` es un filtro **obligatorio**; las zonas de almacenaje siempre pertenecen a una sede física.
- La zona de contingencia (`zonaContingenciaId`) forma una referencia circular autorreferencial; al saturarse una zona, el sistema debe redirigir automáticamente los paquetes a la zona de contingencia si está definida.
- `porcentajeOcupacion` es un **campo calculado** por el servidor usando la métrica más restrictiva: `max(pesoActual/capacidadMax, volumenActual/capacidadMax, contadorPaquetes/capacidadMax)`.
- Una zona con `estado: BLOQUEADO` no acepta nuevos paquetes bajo ninguna circunstancia, independientemente de su ocupación.
- Los sellos de tiempo (`timestamp`, `fechaCreacion`, `fechaActualizacion`) se expresan exclusivamente en **UTC (ISO-8601)** con sufijo `Z`.

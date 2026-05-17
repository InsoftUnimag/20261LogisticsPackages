# API Externa — Consulta de Usuarios, Perfiles y Roles

## Definición del Endpoint

| Propiedad | Valor |
|---|---|
| **URL Producción** | `https://api.corporativo.com/v1/usuarios` |
| **URL Desarrollo** | `http://localhost:8000/api/v1/usuarios` |
| **Método HTTP** | `GET` |
| **Autenticación** | `Bearer Token` (JWT) |
| **Content-Type** | `application/json` |

---

## Estructura de Solicitud (Request)

### Path Variables

| Variable | Tipo | Requerido | Descripción |
|---|---|---|---|
| `{idUsuario}` | `UUID` | No | UUID del usuario específico. Si se omite, retorna listado paginado. |

### Query Parameters

| Parámetro | Tipo | Requerido | Descripción |
|---|---|---|---|
| `page` | `Integer` | Opcional | Número de página (default: `0`) |
| `size` | `Integer` | Opcional | Elementos por página (default: `20`, max: `100`) |
| `sort` | `String` | Opcional | Campo de ordenación (ej. `nombreCompleto,asc`) |
| `username` | `String` | Opcional | Filtro por nombre de usuario (búsqueda parcial) |
| `rol` | `String` | Opcional | Filtro por rol exacto (ver lista de roles abajo) |
| `email` | `String` | Opcional | Filtro por correo electrónico |
| `activo` | `Boolean` | Opcional | Filtro por estado habilitado/deshabilitado |

### Roles Válidos

| Rol | Descripción |
|---|---|
| `ADMIN` | Administrador del sistema con acceso total |
| `EMPLEADO_ENVIO_RECEPCION` | Personal de admisión y recepción de paquetes |
| `ALMACENISTA` | Encargado de zonas de almacenaje y clasificación |
| `CONTROLADOR_NOVEDADES` | Gestor de incidencias y novedades en bodega |
| `OPERADOR_BODEGA` | Operador logístico multipropósito |

### Ejemplo de Llamada

```http
GET /api/v1/usuarios?page=0&size=10&rol=OPERADOR_BODEGA&activo=true
Host: api.corporativo.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

```http
GET /api/v1/usuarios/a1b2c3d4-e5f6-7890-abcd-ef1234567890
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
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "username": "jperez",
      "email": "jperez@logistica.com.co",
      "nombreCompleto": "Juan Pérez Martínez",
      "tipoDocumento": "CEDULA_CIUDADANIA",
      "numeroDocumento": "1012345678",
      "telefono": "+57 300 123 4567",
      "rol": "EMPLEADO_ENVIO_RECEPCION",
      "idSedeAsignada": "550e8400-e29b-41d4-a716-446655440001",
      "nombreSedeAsignada": "Sede Central Bogotá",
      "enabled": true,
      "fechaCreacion": "2025-06-01T10:00:00Z",
      "fechaActualizacion": "2026-05-15T08:30:00Z"
    },
    {
      "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "username": "mlopez",
      "email": "mlopez@logistica.com.co",
      "nombreCompleto": "María López Rodríguez",
      "tipoDocumento": "CEDULA_CIUDADANIA",
      "numeroDocumento": "1023456789",
      "telefono": "+57 310 987 6543",
      "rol": "ALMACENISTA",
      "idSedeAsignada": "550e8400-e29b-41d4-a716-446655440002",
      "nombreSedeAsignada": "Sede Medellín",
      "enabled": true,
      "fechaCreacion": "2025-08-15T14:00:00Z",
      "fechaActualizacion": "2026-04-20T11:45:00Z"
    },
    {
      "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
      "username": "cgonzalez",
      "email": "cgonzalez@logistica.com.co",
      "nombreCompleto": "Carlos González Ruiz",
      "tipoDocumento": "CEDULA_EXTRANJERIA",
      "numeroDocumento": "E-1234567",
      "telefono": "+57 320 555 0101",
      "rol": "CONTROLADOR_NOVEDADES",
      "idSedeAsignada": "550e8400-e29b-41d4-a716-446655440003",
      "nombreSedeAsignada": "Sede Cali",
      "enabled": true,
      "fechaCreacion": "2026-01-10T09:00:00Z",
      "fechaActualizacion": "2026-05-16T14:19:20Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 3,
  "totalPages": 1,
  "last": true
}
```

### Recurso Individual — `200 OK`

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "username": "jperez",
  "email": "jperez@logistica.com.co",
  "nombreCompleto": "Juan Pérez Martínez",
  "tipoDocumento": "CEDULA_CIUDADANIA",
  "numeroDocumento": "1012345678",
  "telefono": "+57 300 123 4567",
  "rol": "EMPLEADO_ENVIO_RECEPCION",
  "idSedeAsignada": "550e8400-e29b-41d4-a716-446655440001",
  "nombreSedeAsignada": "Sede Central Bogotá",
  "enabled": true,
  "fechaCreacion": "2025-06-01T10:00:00Z",
  "fechaActualizacion": "2026-05-15T08:30:00Z"
}
```

---

## Diccionario de Datos

### Campos del Objeto `Usuario`

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id` | `UUID` | Requerido, formato UUID v4 | Identificador único del usuario |
| `username` | `String` | Requerido, único, max 50 chars, alfanumérico | Nombre de usuario para inicio de sesión |
| `email` | `String` | Requerido, único, max 100 chars, formato email válido | Correo electrónico corporativo |
| `nombreCompleto` | `String` | Requerido, max 150 chars | Nombre completo del empleado |
| `tipoDocumento` | `String` | Requerido, enum | Tipo de documento de identidad colombiano: `CEDULA_CIUDADANIA`, `CEDULA_EXTRANJERIA`, `PASAPORTE`, `NIT` |
| `numeroDocumento` | `String` | Requerido, único, max 20 chars | Número de documento de identidad. Para `CEDULA_CIUDADANIA`: 7-10 dígitos numéricos. Para `NIT`: formato `XXX.XXX.XXX-X`. Para `PASAPORTE`: alfanumérico. |
| `telefono` | `String` | Opcional, max 20 chars | Número de contacto telefónico con código de país |
| `rol` | `String` | Requerido, enum | Rol asignado en el sistema logístico. Ver tabla de roles en sección anterior. |
| `idSedeAsignada` | `UUID` | Opcional | UUID de la sede a la que está asignado el usuario |
| `nombreSedeAsignada` | `String` | Opcional, max 150 chars | Nombre descriptivo de la sede asignada (solo lectura) |
| `enabled` | `Boolean` | Requerido | Indica si la cuenta está habilitada para acceso al sistema |
| `fechaCreacion` | `String` | Requerido, ISO-8601 UTC con sufijo `Z` | Marca temporal de creación del registro |
| `fechaActualizacion` | `String` | Opcional, ISO-8601 UTC con sufijo `Z` | Marca temporal de la última actualización |

### Notas sobre Tipos de Documento Colombianos

| Tipo Documento | Formato | Ejemplo Válido | Validación |
|---|---|---|---|
| `CEDULA_CIUDADANIA` | 7 a 10 dígitos numéricos | `1012345678` | Solo dígitos, sin puntos ni guiones |
| `CEDULA_EXTRANJERIA` | Letra `E` seguida de guion y 6-7 dígitos | `E-1234567` | Formato `E-NNNNNNN` |
| `PASAPORTE` | Alfanumérico, 6-12 caracteres | `AB123456` | Letras mayúsculas y números |
| `NIT` | Formato `XXX.XXX.XXX-X` | `900.123.456-7` | Con puntos y guion, dígito de verificación |

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
      "campo": "rol",
      "mensaje": "El valor 'SUPERVISOR' no es válido. Roles permitidos: ADMIN, EMPLEADO_ENVIO_RECEPCION, ALMACENISTA, CONTROLADOR_NOVEDADES, OPERADOR_BODEGA"
    }
  ],
  "path": "/api/v1/usuarios"
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
  "path": "/api/v1/usuarios"
}
```

### `404 Not Found` — Usuario no encontrado por UUID

```json
{
  "timestamp": "2026-05-16T14:19:20Z",
  "status": 404,
  "codigo": "USUARIO_NO_ENCONTRADO",
  "mensaje": "No se encontró un usuario con el ID a1b2c3d4-e5f6-7890-abcd-ef1234567999.",
  "errores": null,
  "path": "/api/v1/usuarios/a1b2c3d4-e5f6-7890-abcd-ef1234567999"
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
  "path": "/api/v1/usuarios"
}
```

---

## Notas Adicionales

- La contraseña (`passwordHash`) **nunca** se expone en las respuestas de la API de consulta. Solo se utiliza internamente para autenticación.
- El campo `idSedeAsignada` permite vincular al usuario con la sede donde opera. Si es `null`, el usuario tiene alcance global (típicamente `ADMIN`).
- Los resultados paginados siguen el formato **Spring Data REST** (`content`, `page`, `size`, `totalElements`, `totalPages`).
- Los sellos de tiempo (`timestamp`, `fechaCreacion`, `fechaActualizacion`) se expresan exclusivamente en **UTC (ISO-8601)** con sufijo `Z`.

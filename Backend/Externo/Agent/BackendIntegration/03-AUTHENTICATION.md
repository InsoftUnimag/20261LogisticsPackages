# Autenticación y Autorización (JWT)

## Arquitectura de Seguridad

El backend utiliza **Spring Security 6.x** con autenticación stateless mediante **JWT (JSON Web Token)**.

No hay sesiones en el servidor. El frontend debe enviar el token JWT en cada petición.

---

## Flujo de Autenticación

```
Frontend                          Backend
   │                                 │
   │  POST /api/auth/login           │
   │  { username, password }         │
   │ ─────────────────────────────►  │
   │                                 │  Verifica credenciales
   │                                 │  Genera JWT (24h exp)
   │  200 { token, username, rol }   │
   │ ◄─────────────────────────────  │
   │                                 │
   │  GET /api/paquetes/1            │
   │  Authorization: Bearer <token>  │
   │ ─────────────────────────────►  │
   │                                 │  Valida JWT
   │                                 │  Extrae usuario/roles
   │  200 { ... datos del paquete }  │
   │ ◄─────────────────────────────  │
```

---

## Cómo obtener un token

### Login

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Respuesta exitosa:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "admin",
  "rol": "ADMIN"
}
```

### Register

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "nuevo_usuario",
  "password": "password123",
  "email": "user@logistics.com",
  "nombreCompleto": "Nombre Apellido",
  "rol": "OPERADOR_BODEGA"
}
```

---

## Cómo enviar el token en cada petición

El token se envía en el header HTTP `Authorization`:

```http
GET http://localhost:8080/api/paquetes/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## Dónde guardar el token en el Frontend

### Opción 1: `localStorage` (Recomendada para SPAs)
```javascript
localStorage.setItem('token', response.data.token);
```
- Persiste aunque se cierre el navegador.
- Se envía automáticamente con cada petición vía interceptor.
- **Riesgo:** vulnerable a XSS. Mitígalo sanitizando inputs.

### Opción 2: `sessionStorage`
```javascript
sessionStorage.setItem('token', response.data.token);
```
- Se borra al cerrar la pestaña/navegador.
- Más seguro que localStorage pero menos persistente.

### Opción 3: HTTP-only Cookie (Más seguro)
- El backend envía el token como cookie `httpOnly` (no accesible desde JavaScript).
- Previene ataques XSS.
- **Requiere cambios en el backend** para enviar cookies en lugar de JSON.

---

## Manejo de Expiración del Token

El token JWT expira en **24 horas** (configurable).

### Estrategia de renovación:

1. **Antes de cada petición**, verifica la expiración:
   ```javascript
   function isTokenExpired(token) {
     const payload = JSON.parse(atob(token.split('.')[1]));
     return payload.exp * 1000 < Date.now();
   }
   ```

2. **Interceptor automático** (con Axios):
   ```javascript
   api.interceptors.response.use(
     (response) => response,
     (error) => {
       if (error.response?.status === 401) {
         localStorage.removeItem('token');
         window.location.href = '/login';
       }
       return Promise.reject(error);
     }
   );
   ```

3. **Refresh automático** (cuando el backend lo soporte):
   - Actualmente no hay endpoint de refresh token.
   - Si el token expira, redirigir al login.
   - El usuario debe iniciar sesión nuevamente.

> **Nota sobre zona horaria:** El backend opera estrictamente en **UTC**. Todas las fechas en los tokens y respuestas están en ISO-8601 con zona UTC (`Z`). El frontend debe convertir a hora local solo para visualización.

> **Nota:** El endpoint `/api/auth/register` también devuelve un token, así que puedes registrar y loguear al usuario en un solo paso.

---

## Usuarios por Defecto (Desarrollo)

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `operador` | `operador123` | OPERADOR_BODEGA |

---

## Resumen para el Frontend

| Concepto | Detalle |
|---|---|
| Endpoint de login | `POST /api/auth/login` |
| Endpoint de registro | `POST /api/auth/register` |
| Header del token | `Authorization: Bearer <token>` |
| Almacenamiento | `localStorage` (recomendado) |
| Expiración | 24 horas |
| Manejo de expiración | Redirigir al login si 401 |
| Roles disponibles | `ADMIN`, `OPERADOR_BODEGA` |
| Usuarios test | `admin/admin123`, `operador/operador123` |

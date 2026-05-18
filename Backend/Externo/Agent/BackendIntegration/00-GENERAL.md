# Plan General de Conexión Frontend → Backend

## Instrucciones para el Agente de IA

Este documento es la guía maestra para conectar una aplicación frontend (React, Vue, Angular, etc.) con el backend de **Logistics & Packages API**. Léelo completamente antes de actuar. Luego, para cada tema específico, consulta el documento dedicado.

---

## 1. Arquitectura General

```
[Frontend (React/Vue/Angular)]
        │
        ├── Peticiones HTTP (JSON)
        ├── Token JWT en Header (Authorization: Bearer <token>)
        ├── CORS pre-flight (OPTIONS)
        │
        ▼
[Backend (Spring Boot 3.2.5 + Java 21)]
        │
        ├── SecurityFilterChain (JWT Validation)
        ├── Controladores REST (/api/paquetes, /route, /api/auth)
        ├── PostgreSQL (Persistencia)
        └── AWS SQS/S3 (Mensajería asíncrona y almacenamiento)
```

## 2. Servicios Disponibles

| Servicio | Base URL | Autenticación | Documento |
|---|---|---|---|
| Autenticación | `/api/auth/` | No requiere (público) | `03-AUTHENTICATION.md` |
| Paquetes (CRUD + Pesaje + Almacenaje + Clasificación + Novedades) | `/api/paquetes/` | Requiere JWT | `01-API-CONTRACT.md` |
| Consultas Financieras | `/route/` | Requiere JWT | `01-API-CONTRACT.md` |
| Documentación Swagger | `/swagger-ui.html` | No requiere | `01-API-CONTRACT.md` |

## 3. Plan de Conexión (Paso a Paso)

### Paso 1: Obtener la URL del Backend
- **Desarrollo local:** `http://localhost:8080`
- La URL **nunca debe ir hardcodeada** en el código. Usa variables de entorno (ver `06-ENV-VARIABLES.md`).

### Paso 2: Configurar CORS
- El backend ya acepta peticiones desde `http://localhost:3000` (por defecto).
- Si tu frontend corre en otro puerto/dominio, actualiza la variable `CORS_ALLOWED_ORIGINS` en el backend.
- Detalles en `02-CORS.md`.

### Paso 3: Autenticación (Obtener el Token)
1. El frontend envía `POST /api/auth/login` con `username` y `password`.
2. El backend responde con un `JWT` (JSON Web Token).
3. El frontend **guarda el token** en `localStorage` o `sessionStorage`.
4. El token se envía en **cada petición subsecuente** en el header `Authorization: Bearer <token>`.
5. Cuando el token expire (24h), redirigir al login.
6. Detalles en `03-AUTHENTICATION.md`.

### Paso 4: Elegir un Cliente HTTP
- Usa **Axios** (recomendado) con un interceptor que inyecte el token automáticamente.
- Detalles en `07-HTTP-CLIENT.md`.

### Paso 5: Configurar Variables de Entorno
- Crea un archivo `.env` en la raíz del frontend:
  ```env
  VITE_API_BASE_URL=http://localhost:8080
  ```
- Detalles en `06-ENV-VARIABLES.md`.

### Paso 6: Manejar Estados de Carga y Error
- Toda petición debe considerar: **loading → éxito → error**.
- No dejes la pantalla en blanco mientras cargan datos.
- Detalles en `05-UX-NETWORK.md`.

### Paso 7: Gestionar el Estado (State Management)
- Decide si los datos van a estado local (componente) o global (Redux/Context/Zustand).
- Detalles en `04-STATE-MANAGEMENT.md`.

---

## 4. Resumen de Endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/auth/login` | Iniciar sesión (público) |
| `POST` | `/api/auth/register` | Registrar usuario (público) |
| `POST` | `/api/paquetes/admision` | Registrar admisión de paquete |
| `GET` | `/api/paquetes/{id}` | Consultar paquete por ID |
| `POST` | `/api/paquetes/pesaje` | Procesar pesaje |
| `GET` | `/api/paquetes/pesaje/health` | Health check pesaje |
| `GET` | `/api/paquetes/{id}/almacenaje/sugerencia` | Sugerencia de zona de almacenaje |
| `POST` | `/api/paquetes/{id}/almacenaje` | Asignar zona de almacenaje |
| `GET` | `/api/paquetes/clasificacion/sugerencia/{id}` | Sugerencia de clasificación |
| `POST` | `/api/paquetes/clasificacion/confirmar` | Confirmar clasificación |
| `POST` | `/api/paquetes/{id}/novedades` | Reportar novedad (multipart) |
| `GET` | `/route/{idRuta}/package/{idPaquete}` | Consulta financiera de ruta |

---

## 5. Códigos de Estado HTTP (Comportamiento Esperado)

| Código | Significado | Acción del Frontend |
|---|---|---|
| `200 OK` | Éxito | Procesar respuesta JSON |
| `201 Created` | Recurso creado | Redirigir o mostrar confirmación |
| `400 Bad Request` | Datos inválidos | Mostrar errores de validación al usuario |
| `401 Unauthorized` | Token faltante o inválido | Redirigir al login |
| `403 Forbidden` | Sin permisos | Mostrar mensaje de acceso denegado |
| `404 Not Found` | Recurso no existe | Mostrar mensaje adecuado |
| `409 Conflict` | Concurrencia/duplicado | Reintentar o notificar al usuario |
| `500 Internal Server Error` | Error del servidor | Mostrar mensaje genérico amigable |

---

## 6. Documentos Relacionados

| Documento | Contenido |
|---|---|
| `01-API-CONTRACT.md` | Esquemas JSON, endpoints detallados, códigos de respuesta |
| `02-CORS.md` | Configuración CORS y pre-flight |
| `03-AUTHENTICATION.md` | Flujo JWT, login, registro, manejo de expiración |
| `04-STATE-MANAGEMENT.md` | Estado local vs global, caché, ciclo de vida |
| `05-UX-NETWORK.md` | Loading states, errores, optimistic updates |
| `06-ENV-VARIABLES.md` | Variables de entorno, `.env` file |
| `07-HTTP-CLIENT.md` | Axios interceptors, Fetch API, manejo de peticiones |

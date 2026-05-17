# Variables de Entorno (Environment Variables)

## Principio

**Nunca hardcodees la URL del backend ni ninguna configuración sensible en el código fuente.**

Usa archivos `.env` para que la misma base de código funcione en desarrollo, staging y producción sin modificar nada.

---

## Archivo `.env` en el Frontend

Crea un archivo `.env` en la raíz de tu proyecto frontend:

```env
# ============================================
# Logistics & Packages - Frontend Config
# ============================================

# URL Base de la API del Backend
VITE_API_BASE_URL=http://localhost:8080

# Timeout de peticiones HTTP en milisegundos
VITE_API_TIMEOUT=15000

# Nombre del storage key para el token JWT
VITE_TOKEN_KEY=auth_token
```

### Reglas según el framework:

| Framework | Prefijo de variables | Cómo se accede |
|---|---|---|
| **Vite** | `VITE_` | `import.meta.env.VITE_API_BASE_URL` |
| **Create React App** | `REACT_APP_` | `process.env.REACT_APP_API_BASE_URL` |
| **Next.js** | `NEXT_PUBLIC_` | `process.env.NEXT_PUBLIC_API_BASE_URL` |
| **Vue CLI** | `VUE_APP_` | `process.env.VUE_APP_API_BASE_URL` |

---

## Archivos de Entorno por Entorno

Crea diferentes archivos para cada entorno:

| Archivo | Entorno | Propósito |
|---|---|---|
| `.env` | Desarrollo | Valores por defecto |
| `.env.development` | Desarrollo | Sobrescribe `.env` en `npm run dev` |
| `.env.staging` | Staging | URL de staging |
| `.env.production` | Producción | URL de producción |
| `.env.local` | Local | **No se sube a git** - credenciales locales |

### Ejemplo `.env.production`:
```env
VITE_API_BASE_URL=https://api.logistics.com
VITE_API_TIMEOUT=30000
```

### Ejemplo `.env.staging`:
```env
VITE_API_BASE_URL=https://staging-api.logistics.com
VITE_API_TIMEOUT=20000
```

---

## .gitignore

Asegúrate de que `.env.local` esté en `.gitignore`:

```gitignore
# Environment variables - local overrides
.env.local
.env.*.local
```

Los archivos `.env`, `.env.development`, `.env.production` **SÍ** se suben al repo (contienen configuración, no secretos).

---

## Uso en el Código del Frontend

### Axios instance con variables de entorno:

```javascript
// api.js
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: parseInt(import.meta.env.VITE_API_TIMEOUT || '15000'),
  headers: {
    'Content-Type': 'application/json'
  }
});

export default api;
```

### Auth service con variables de entorno:

```javascript
// auth.js
const TOKEN_KEY = import.meta.env.VITE_TOKEN_KEY || 'auth_token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY);
}
```

---

## Variables del Backend (para referencia)

El backend también usa variables de entorno. Las más relevantes para el frontend:

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos por CORS | `http://localhost:3000` |
| `JWT_SECRET` | Clave secreta para firmar tokens | (valor base64 interno) |
| `JWT_EXPIRATION_MS` | Duración del token en ms | `86400000` (24h) |

---

## Resumen para el Frontend

| Variable | Descripción | Default | Requerida |
|---|---|---|---|
| `VITE_API_BASE_URL` | URL del backend | `http://localhost:8080` | Sí |
| `VITE_API_TIMEOUT` | Timeout en ms | `15000` | No |
| `VITE_TOKEN_KEY` | Key en localStorage | `auth_token` | No |

### Inicialización mínima:

```env
VITE_API_BASE_URL=http://localhost:8080
```

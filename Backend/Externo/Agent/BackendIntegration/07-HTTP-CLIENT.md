# Cliente de Peticiones HTTP

## Opciones Disponibles

| Opción | Ventajas | Desventajas |
|---|---|---|
| **Fetch API** | Nativo, sin dependencias | Más código manual, errores poco intuitivos |
| **Axios** | Interceptors, transformación automática JSON, mejor manejo de errores | Dependencia externa |

**Recomendación:** Usa **Axios** por sus interceptors que facilitan inyectar el token JWT automáticamente.

---

## Implementación Recomendada con Axios

### 1. Instalación

```bash
npm install axios
```

### 2. Crear instancia base

```javascript
// api.js
import axios from 'axios';
import { getToken, removeToken } from './auth';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: parseInt(import.meta.env.VITE_API_TIMEOUT || '15000'),
  headers: {
    'Content-Type': 'application/json'
  }
});
```

### 3. Interceptor de Petición (Inyectar Token JWT)

Este interceptor se ejecuta **antes de cada petición** y agrega el token automáticamente:

```javascript
api.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);
```

### 4. Interceptor de Respuesta (Manejar Errores Globales)

Este interceptor se ejecuta **después de cada respuesta**. Detecta errores globales como 401 (token expirado):

```javascript
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      removeToken();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 5. Auth Service (Login y Token)

```javascript
// auth.js
import api from './api';

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

export async function login(username, password) {
  const response = await api.post('/api/auth/login', { username, password });
  const { token, username: user, rol } = response.data;
  setToken(token);
  return { username: user, rol };
}

export async function register(userData) {
  const response = await api.post('/api/auth/register', userData);
  const { token, username, rol } = response.data;
  setToken(token);
  return { username, rol };
}

export function logout() {
  removeToken();
  window.location.href = '/login';
}
```

### 6. Paquete Service (Ejemplo de CRUD)

```javascript
// paqueteService.js
import api from './api';

export const paqueteService = {
  list(params = {}) {
    return api.get('/api/paquetes', { params });
  },

  getById(id) {
    return api.get(`/api/paquetes/${id}`);
  },

  registerAdmision(data) {
    return api.post('/api/paquetes/admision', data);
  },

  processPesaje(data) {
    return api.post('/api/paquetes/pesaje', data);
  },

  getStorageSuggestion(paqueteId) {
    return api.get(`/api/paquetes/${paqueteId}/almacenaje/sugerencia`);
  },

  assignStorage(paqueteId, data) {
    return api.post(`/api/paquetes/${paqueteId}/almacenaje`, data);
  },

  getClassificationSuggestion(paqueteId) {
    return api.get(`/api/paquetes/clasificacion/sugerencia/${paqueteId}`);
  },

  confirmClassification(data) {
    return api.post('/api/paquetes/clasificacion/confirmar', data);
  },

  reportNovedad(paqueteId, formData) {
    return api.post(`/api/paquetes/${paqueteId}/novedades`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

};
```

---

## Servicio de Login Completo (con manejo de errores)

```javascript
import api from './api';

export async function login(username, password) {
  try {
    const response = await api.post('/api/auth/login', { username, password });
    const { token, username: user, rol } = response.data;

    localStorage.setItem('auth_token', token);

    return {
      success: true,
      user: { username: user, rol },
      token
    };
  } catch (error) {
    const status = error.response?.status;
    const mensaje = error.response?.data?.mensaje || 'Error al conectar con el servidor';

    if (!error.response) {
      return { success: false, error: 'No hay conexión con el servidor. Verifica tu conexión de internet.' };
    }

    if (status === 401) {
      return { success: false, error: 'Usuario o contraseña incorrectos' };
    }

    return { success: false, error: mensaje };
  }
}
```

---

## Alternativa: Fetch API

Si prefieres no instalar Axios:

```javascript
const API_BASE = import.meta.env.VITE_API_BASE_URL;

async function apiFetch(endpoint, options = {}) {
  const token = localStorage.getItem('auth_token');

  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
      ...options.headers
    },
    ...options
  };

  const response = await fetch(`${API_BASE}${endpoint}`, config);

  if (!response.ok) {
    if (response.status === 401) {
      localStorage.removeItem('auth_token');
      window.location.href = '/login';
    }
    const error = await response.json().catch(() => ({}));
    throw new Error(error.mensaje || `Error ${response.status}`);
  }

  return response.json();
}

// Uso:
// const paquete = await apiFetch('/api/paquetes/123');
// const login = await apiFetch('/api/auth/login', {
//   method: 'POST',
//   body: JSON.stringify({ username, password })
// });
```

---

## Paginación (Listar Paquetes)

```javascript
async function listarPaquetes(page = 0, size = 20, filters = {}) {
  const params = { page, size, ...filters };
  const response = await api.get('/api/paquetes', { params });
  return response.data; // { content, page, size, totalElements, totalPages, ... }
}

// Uso con filtros:
const result = await listarPaquetes(0, 20, {
  estado: 'RECIBIDO_EN_SEDE',
  fechaDesde: '2026-05-01',
  fechaHasta: '2026-05-13'
});
```

---

## Pre-validación de UUID en el Frontend

Antes de enviar un ID al backend, valida que tenga formato UUID para evitar peticiones innecesarias:

```javascript
const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function isValidUUID(value) {
  return UUID_REGEX.test(value);
}

// Uso en un servicio:
function getPaquetePorId(id) {
  if (!isValidUUID(id)) {
    return Promise.reject(new Error('Formato de UUID inválido'));
  }
  return api.get(`/api/paquetes/${id}`);
}
```

---

## Tipado Correcto del Timeout

Asegúrate de que `VITE_API_TIMEOUT` se convierta explícitamente a número para evitar errores de tipo:

```javascript
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: parseInt(import.meta.env.VITE_API_TIMEOUT || '15000', 10), // <-- parseInt obligatorio
  headers: {
    'Content-Type': 'application/json'
  }
});
```

---

## Subida de Archivos (Novedades - multipart/form-data)

> **Importante:** Los campos de texto se envían como campos individuales del formulario (`@ModelAttribute`), **no** como JSON. El archivo se envía como `MultipartFile` separado.

```javascript
const formData = new FormData();
formData.append('tipoNovedad', 'DAÑADO');
formData.append('descripcion', 'Paquete con abolladura en esquina superior');
formData.append('evidencia', archivoSeleccionado); // File object

// Con Axios:
await api.post(`/api/paquetes/${paqueteId}/novedades`, formData, {
  headers: { 'Content-Type': 'multipart/form-data' },
  onUploadProgress: (progressEvent) => {
    const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
    console.log(`Subiendo: ${percent}%`);
  }
});
```

---

## Resumen para el Frontend

| Componente | Archivo | Propósito |
|---|---|---|
| Axios instance | `api.js` | Base URL, timeout, headers por defecto |
| Request interceptor | `api.js` | Inyectar token JWT automáticamente |
| Response interceptor | `api.js` | Detectar 401, redirigir al login |
| Auth service | `auth.js` | Login, register, logout, token management |
| Paquete service | `paqueteService.js` | CRUD de paquetes y operaciones relacionadas |

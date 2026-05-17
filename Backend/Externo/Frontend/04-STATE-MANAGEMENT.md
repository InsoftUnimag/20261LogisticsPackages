# Gestión de Estado y Datos

## Principio General

El backend es **stateless** (sin estado de sesión). Cada petición del frontend debe incluir la información necesaria (token JWT + datos) para que el backend la procese.

El frontend es responsable de mantener y gestionar el estado de la aplicación.

---

## Estado Local vs Global

### ¿Qué va en estado local?

Datos que solo necesita **un componente** o una **página específica**:

- Resultado de una búsqueda que solo se muestra una vez
- Formulario de registro de paquete
- Estado de carga de una tabla

**Ejemplo (React useState):**
```javascript
const [paquete, setPaquete] = useState(null);
const [loading, setLoading] = useState(true);
```

### ¿Qué va en estado global?

Datos que necesitan **múltiples componentes** en toda la app:

- **Token JWT** → Todas las peticiones lo necesitan
- **Usuario autenticado** → Barra de navegación, perfil
- **Lista de paquetes** → Si se muestra en varias pantallas

---

## Estrategia Recomendada: Context API + Hooks

Para una aplicación de logística, se recomienda:

### 1. AuthContext (Estado Global)

```javascript
// AuthContext.jsx
const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));

  const login = async (username, password) => {
    const response = await api.post('/api/auth/login', { username, password });
    localStorage.setItem('token', response.data.token);
    setToken(response.data.token);
    setUser({ username: response.data.username, rol: response.data.rol });
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
}
```

### 2. Servicios API (Sin Estado Global)

Cada módulo tiene su propio hook que maneja estado local:

> **Nota sobre paginación:** El endpoint `GET /api/paquetes` devuelve una página. El estado de la paginación (página actual, tamaño, total de elementos) debe mantenerse localmente en el componente que renderiza la tabla. Considera cachear páginas ya visitadas para evitar viajes innecesarios al servidor.

```javascript
// usePaquetes.js
export function usePaquetes() {
  const [paquetes, setPaquetes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchPaquete = async (id) => {
    setLoading(true);
    try {
      const response = await api.get(`/api/paquetes/${id}`);
      setPaquetes(response.data);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  return { paquetes, loading, error, fetchPaquete };
}
```

---

## Ciclo de Vida de las Peticiones

Cada vez que un componente necesita datos, debe considerar:

```
Componente Montado
    │
    ▼
Iniciar Loading (spinner)
    │
    ▼
Hacer Petición HTTP (con token)
    │
    ├── Éxito → Guardar datos → Mostrar UI
    │
    └── Error → Mostrar mensaje de error (no pantalla en blanco)
```

### Ejemplo con React useEffect:

```javascript
useEffect(() => {
  fetchPaquete(id);
}, [id]);
```

---

## Caché de Datos

No implementamos caché compleja en el backend. El frontend puede implementar:

### Caché simple en memoria:
```javascript
const cache = {};

async function getPaquete(id) {
  if (cache[id]) return cache[id];
  const response = await api.get(`/api/paquetes/${id}`);
  cache[id] = response.data;
  return response.data;
}
```

### Caché con tiempo de expiración:
```javascript
const cache = {};

async function getPaquete(id) {
  if (cache[id] && Date.now() - cache[id].timestamp < 30000) {
    return cache[id].data;
  }
  const response = await api.get(`/api/paquetes/${id}`);
  cache[id] = { data: response.data, timestamp: Date.now() };
  return response.data;
}
```

---

## Datos Asíncronos (Mensajería)

Algunas operaciones del backend son **asíncronas** (ej. asignación de ruta vía RabbitMQ). Esto significa que:

1. El frontend envía una petición
2. El backend responde inmediatamente con un acuse de recibo
3. El procesamiento real ocurre después (segundos o minutos)
4. El frontend debe **consultar periódicamente** o usar **WebSocket/SSE** para obtener el resultado

Actualmente no hay WebSocket implementado. Para verificar el estado actualizado:

```javascript
// Polling cada 5 segundos
const interval = setInterval(async () => {
  const paquete = await api.get(`/api/paquetes/${id}`);
  if (paquete.data.estado === 'LISTO_PARA_DESPACHO') {
    clearInterval(interval);
    // Mostrar resultado al usuario
  }
}, 5000);
```

---

## Resumen para el Frontend

| Concepto | Recomendación |
|---|---|
| Estado global mínimo | Token + Usuario autenticado |
| Estado local | Datos de paquetes, formularios, UI states |
| Caché | Simple en memoria con expiración (30s) |
| Polling | Para operaciones asíncronas (cada 5s) |
| Herramientas | Context API + Custom Hooks (o Zustand si prefieres) |

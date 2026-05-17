# Configuración CORS (Cross-Origin Resource Sharing)

## ¿Qué es CORS?

El navegador bloquea peticiones HTTP desde un origen (dominio, protocolo, puerto) diferente al del servidor. CORS es el mecanismo que permite al servidor decir "confío en este origen" y levantar el bloqueo.

---

## Estado Actual del Backend

El backend ya tiene CORS configurado para aceptar peticiones desde:

```
http://localhost:3000
```

Puedes cambiar este valor mediante la variable de entorno:

```
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

O múltiples orígenes separados por coma:

```
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

### Configuración actual (application.yml):

```yaml
app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
```

### Métodos HTTP permitidos:
- `GET`
- `POST`
- `PUT`
- `DELETE`
- `OPTIONS`

### Headers permitidos:
- `Content-Type`
- `Authorization`
- `X-Requested-With`

### Credenciales:
- `allowCredentials: true` (permite cookies y tokens)

---

## Flujo CORS (Pre-flight)

El navegador no envía una petición "real" directamente cuando hay cambio de origen. Primero envía una petición **OPTIONS** (pre-flight):

```
OPTIONS /api/paquetes/1
Origin: http://localhost:3000
Access-Control-Request-Method: GET
```

El backend responde con:

```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With
Access-Control-Allow-Credentials: true
```

Solo después de recibir esta respuesta afirmativa, el navegador envía la petición `GET` real.

---

## Configuración del Frontend

No necesitas configurar nada especial en el frontend para CORS. El navegador maneja esto automáticamente.

**IMPORTANTE:** Durante desarrollo, asegúrate de que la URL del backend en tu `.env` coincida exactamente con el origen permitido por CORS (incluyendo protocolo, dominio y puerto).

---

## Troubleshooting CORS

### Error típico en consola del navegador:
```
Access to fetch at 'http://localhost:8080/api/paquetes/1' from origin 'http://localhost:3000' has been blocked by CORS policy
```

### Causas comunes:
1. **Puerto incorrecto:** El frontend corre en `:3000` pero CORS está configurado para otro puerto.
   - Solución: Actualizar `CORS_ALLOWED_ORIGINS` a `http://localhost:3000`.

2. **Header personalizado no permitido:** Si el frontend envía un header como `X-Custom-Header`, este debe estar incluido en `allowedHeaders`.

3. **Protocolo mismatch:** `http://localhost` vs `https://localhost`. Deben coincidir exactamente.

4. **Petición con credenciales:** Si `credentials: "include"` se usa en fetch, el backend debe responder con `Access-Control-Allow-Credentials: true` y el `Access-Control-Allow-Origin` no puede ser `*`.

---

## Resumen para el Frontend

| Aspecto | Detalle |
|---|---|
| Origen por defecto | `http://localhost:3000` |
| Cómo cambiar origen | Variable de entorno `CORS_ALLOWED_ORIGINS` en el backend |
| Métodos permitidos | GET, POST, PUT, DELETE, OPTIONS |
| Headers permitidos | Content-Type, Authorization, X-Requested-With |
| Credenciales | Permitidas (allowCredentials: true) |
| Configuración extra en frontend | Ninguna (el navegador maneja CORS automáticamente) |

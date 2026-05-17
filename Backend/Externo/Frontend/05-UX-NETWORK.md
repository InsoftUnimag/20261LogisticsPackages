# Experiencia de Usuario en la Red (UX)

## Principio Fundamental

**La red es lenta e impredecible.** El frontend debe prepararse para tres estados en cada interacción con el backend:

1. **Loading** (mientras espera la respuesta)
2. **Success** (cuando los datos llegan)
3. **Error** (cuando algo falla)

> **NUNCA dejes la pantalla en blanco mientras cargan datos.**

---

## 1. Estados de Carga (Loading States)

### Indicadores visuales necesarios:

| Situación | Componente UX |
|---|---|
| Carga inicial de página | Skeleton screen (esqueleto) |
| Petición a una tabla | Spinner en la tabla |
| Envío de formulario | Botón deshabilitado con spinner |
| Búsqueda/filtro | Indicador en el campo de búsqueda |

### Ejemplo con React:

```javascript
function PaqueteDetail({ id }) {
  const { paquete, loading, error } = usePaquete(id);

  if (loading) return <Skeleton variant="rectangular" width="100%" height={200} />;
  if (error) return <ErrorAlert message="No pudimos cargar el paquete" onRetry={() => fetchPaquete(id)} />;

  return <PaqueteCard paquete={paquete} />;
}
```

### Recomendaciones de UI:

- **Skeleton Screens:** Muestra la estructura de la página con rectángulos grises animados (shimmer).
- **Spinners:** Solo para operaciones rápidas (< 2s). Para operaciones largas, usa barra de progreso.
- **Botón de envío:** Deshabilitar y mostrar spinner mientras se procesa. Previene doble envío.

---

## 2. Manejo de Errores

### Errores de Red (sin conexión):
```javascript
try {
  const response = await api.get('/api/paquetes');
} catch (error) {
  if (!error.response) {
    // Error de red (sin conexión, timeout)
    showToast('No hay conexión con el servidor. Verifica tu internet.');
  }
}
```

### Errores HTTP (respuesta del servidor):
```javascript
catch (error) {
  const status = error.response?.status;
  const mensaje = error.response?.data?.mensaje || 'Error inesperado';

  switch (status) {
    case 400:
      showValidationErrors(error.response.data); // Errores de formulario
      break;
    case 401:
      redirectToLogin(); // Token expirado o inválido
      break;
    case 403:
      showToast('No tienes permisos para realizar esta acción');
      break;
    case 404:
      showToast('El recurso solicitado no existe');
      break;
    case 409:
      showToast('Conflicto: otro usuario modificó este recurso. Recarga e intenta de nuevo.');
      break;
    case 500:
      showToast('Error interno del servidor. Intenta más tarde.');
      break;
    default:
      showToast(mensaje);
  }
}
```

### Componente de Error Reintentable:
```javascript
function ErrorAlert({ message, onRetry }) {
  return (
    <div className="error-container">
      <p>⚠️ {message}</p>
      <button onClick={onRetry}>Intentar de nuevo</button>
    </div>
  );
}
```

---

## 3. Optimistic Updates

Para acciones rápidas (cambiar estado, actualizar datos), puedes actualizar la UI **antes** de que el servidor confirme:

```javascript
async function marcarComoEntregado(paqueteId) {
  // 1. Actualizar UI inmediatamente
  setPaquetes(prev => prev.map(p =>
    p.id === paqueteId ? { ...p, estado: 'ENTREGADO' } : p
  ));

  try {
    // 2. Enviar petición al servidor
    await api.post(`/api/paquetes/${paqueteId}/entregar`);
  } catch (error) {
    // 3. Revertir si falla
    setPaquetes(prev => prev.map(p =>
      p.id === paqueteId ? { ...p, estado: p.estadoAnterior || p.estado } : p
    ));
    showToast('Error al actualizar el estado. Revirtiendo cambios.');
  }
}
```

> **Cuándo usarlo:** Solo para acciones donde el usuario espera feedback instantáneo y la reversión es aceptable.

---

## 4. Timeouts

Configura un timeout global para peticiones. Si el servidor no responde en X segundos, la petición se cancela y se notifica al usuario.

```javascript
// Axios - timeout global
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000, // 15 segundos
  headers: { 'Content-Type': 'application/json' }
});
```

---

## 5. Estrategia de Feedback por Operación

| Operación | Feedback al Usuario |
|---|---|---|
| Cargar lista paginada de paquetes | Skeleton screen + indicador de página actual |
| Navegar entre páginas | Spinner en la tabla + deshabilitar botones de paginación |
| Filtrar por estado/fecha | Spinner en la tabla + mantener filtros visibles |
| Buscar paquete | Spinner + "Buscando..." |
| Registrar admisión | Botón "Guardando..." + toast de éxito |
| Error de registro | Toast con mensaje de error + mantener datos del formulario |
| Token expirado | Redirigir al login con mensaje "Tu sesión expiró" |
| Subir evidencia (novedad) | Barra de progreso de upload + miniatura |
| Operación asíncrona (ruta) | "Solicitud enviada. Esto puede tomar unos segundos." + polling |

---

## 6. Resumen para el Frontend

| Concepto | Acción |
|---|---|
| Loading | Skeleton screens (no spinners genéricos) |
| Error | Mostrar mensaje amigable + botón de reintento |
| Sin conexión | Detectar y mostrar mensaje específico |
| Timeout | 15 segundos global, notificar al usuario |
| Optimistic updates | Usar con cuidado, siempre revertir si falla |
| Doble envío | Deshabilitar botón mientras se procesa |
| Token expirado | Detectar 401, redirigir al login sin perder datos |

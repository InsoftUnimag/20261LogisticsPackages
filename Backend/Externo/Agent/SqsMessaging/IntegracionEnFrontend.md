# Integración Frontend — Mensajería Asíncrona SQS

## Contexto Asíncrono

El backend (M1) utiliza **AWS SQS** para comunicación con Módulo 2 (Rutas) y Módulo 3 (Finanzas). Esto implica que **ninguna operación que involucre a M2 o M3 responde en el mismo request HTTP**.

```
Cliente HTTP → M1 API → SQS (solicitudes-ruta-queue) → M2 procesa
                                                              ↓
Cliente HTTP ← M1 API ← SQS (respuestas-ruta-queue) ← M2 responde
                                 (minutos/horas después)
```

El frontend **nunca debe esperar una respuesta sincrónica** de operaciones como:
- Solicitar una ruta para un paquete
- Obtener eventos de tracking en tiempo real
- Conocer el resultado de una liquidación financiera

---

## Estrategia 1: Polling Cliente (Simplicidad)

### Descripción
El frontend consulta periódicamente un endpoint REST que devuelve el estado actual del paquete.

### Endpoint
```
GET /api/paquetes/{id}/estado
Response: { id, estado, rutaId, fechaUltimaActualizacion, eventos [...] }
```

### Implementación Frontend (React + Clean Code)

```typescript
// hooks/usePaqueteEstado.ts
import { useState, useEffect, useCallback } from 'react';

interface EstadoPaquete {
  id: string;
  estado: string;
  rutaId: string | null;
  fechaUltimaActualizacion: string;
}

interface UsePaqueteEstadoOptions {
  intervaloMs?: number;
  enabled?: boolean;
}

export function usePaqueteEstado(
  paqueteId: string | null,
  options: UsePaqueteEstadoOptions = {}
) {
  const { intervaloMs = 10000, enabled = true } = options;
  const [estado, setEstado] = useState<EstadoPaquete | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [cargando, setCargando] = useState(false);

  const fetchEstado = useCallback(async () => {
    if (!paqueteId) return;
    setCargando(true);
    try {
      const res = await fetch(`/api/paquetes/${paqueteId}/estado`);
      if (!res.ok) throw new Error(`Error ${res.status}`);
      const data = await res.json();
      setEstado(data);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Error desconocido');
    } finally {
      setCargando(false);
    }
  }, [paqueteId]);

  useEffect(() => {
    if (!enabled || !paqueteId) return;
    fetchEstado();
    const id = setInterval(fetchEstado, intervaloMs);
    return () => clearInterval(id);
  }, [enabled, paqueteId, intervaloMs, fetchEstado]);

  return { estado, error, cargando, refetch: fetchEstado };
}
```

### Ventajas
- Implementación simple y universal
- Funciona con cualquier infraestructura (no requiere WebSockets/SSE)
- Fácil de depurar

### Desventajas
- Latencia determinada por el intervalo de polling
- Tráfico HTTP innecesario cuando no hay cambios
- No escalable para cientos de paquetes en tiempo real

---

## Estrategia 2: Server-Sent Events (Recomendada para Tracking)

### Descripción
El frontend se suscribe a un stream de eventos enviados por el servidor cuando ocurren cambios de estado.

### Endpoint Backend (Spring Boot)
```java
@RestController
@RequestMapping("/api/paquetes")
public class PaqueteEventoController {

    private final SseEmitterService sseService;

    @GetMapping("/eventos/stream")
    public SseEmitter streamEventos() {
        return sseService.crearEmitter();
    }
}
```

### Implementación Servicio SSE
```java
@Service
public class SseEmitterService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter crearEmitter() {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 min timeout
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        return emitter;
    }

    public void emitirEvento(String paqueteId, String tipo, Object datos) {
        SseEventBuilder event = SseEmitter.event()
            .id(paqueteId)
            .name(tipo)
            .data(datos);
        emitters.forEach(e -> {
            try { e.send(event); }
            catch (IOException ex) { emitters.remove(e); }
        });
    }
}
```

### Implementación Frontend (React + Clean Code)

```typescript
// hooks/usePaqueteStream.ts
import { useEffect, useRef, useState } from 'react';

interface EventoPaquete {
  tipo: string;
  paqueteId: string;
  datos: unknown;
}

interface UsePaqueteStreamOptions {
  onEvento?: (evento: EventoPaquete) => void;
  reconexionMs?: number;
  enabled?: boolean;
}

export function usePaqueteStream(
  options: UsePaqueteStreamOptions = {}
) {
  const { reconexionMs = 3000, enabled = true } = options;
  const [conectado, setConectado] = useState(false);
  const eventSourceRef = useRef<EventSource | null>(null);
  const [ultimoEvento, setUltimoEvento] = useState<EventoPaquete | null>(null);

  useEffect(() => {
    if (!enabled) return;

    function conectar() {
      const es = new EventSource('/api/paquetes/eventos/stream');

      es.onopen = () => setConectado(true);

      es.addEventListener('PAQUETE_ACTUALIZADO', (e) => {
        const evento: EventoPaquete = {
          tipo: e.type,
          paqueteId: e.lastEventId,
          datos: JSON.parse(e.data),
        };
        setUltimoEvento(evento);
        options.onEvento?.(evento);
      });

      es.addEventListener('NOVEDAD_CRITICA', (e) => {
        const evento: EventoPaquete = {
          tipo: e.type,
          paqueteId: e.lastEventId,
          datos: JSON.parse(e.data),
        };
        setUltimoEvento(evento);
        options.onEvento?.(evento);
      });

      es.onerror = () => {
        setConectado(false);
        es.close();
        setTimeout(conectar, reconexionMs);
      };

      eventSourceRef.current = es;
    }

    conectar();

    return () => {
      eventSourceRef.current?.close();
      setConectado(false);
    };
  }, [enabled, reconexionMs, options.onEvento]);

  return { conectado, ultimoEvento };
}
```

### Ventajas
- Baja latencia (eventos en tiempo real)
- Conexión unidireccional eficiente (HTTP nativo)
- Reconexión automática nativa en navegadores
- Menor overhead que WebSocket para tracking unidireccional

### Desventajas
- No soportado en algunos proxies/CDNs antiguos
- Solo comunicación servidor → cliente
- Límite de conexiones simultáneas por navegador (6 por dominio)

---

## Estrategia 3: WebSockets (Para Novedades Críticas)

### Descripción
Conexión bidireccional para eventos que requieren interacción en tiempo real (notificaciones de novedades graves, alertas).

### Implementación Backend (STOMP + Spring)
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-paquetes")
            .setAllowedOrigins("*")
            .withSockJS();
    }
}
```

### Implementación Frontend (React)

```typescript
// hooks/useWebSocketNovedades.ts
import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';

export function useWebSocketNovedades(paqueteId: string | null) {
  const [novedades, setNovedades] = useState<any[]>([]);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!paqueteId) return;

    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws-paquetes',
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/novedades/${paqueteId}`, (msg) => {
          setNovedades(prev => [...prev, JSON.parse(msg.body)]);
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => client.deactivate();
  }, [paqueteId]);

  return { novedades };
}
```

### Ventajas
- Comunicación bidireccional (servidor puede iniciar, cliente puede enviar)
- Baja latencia
- Soporte nativo en navegadores modernos

### Desventajas
- Mayor complejidad de implementación y despliegue
- Stateful (requiere sticky sessions o Redis en cluster)
- Overhead de conexión (handshake HTTP → WebSocket)

---

## Comparativa y Recomendación

| Criterio | Polling | SSE | WebSocket |
|----------|---------|-----|-----------|
| Latencia | 10-30s (según intervalo) | < 1s | < 100ms |
| Dirección | Cliente → Servidor | Servidor → Cliente | Bidireccional |
| Complejidad Frontend | Baja | Media | Alta |
| Complejidad Backend | Baja | Media | Alta |
| Reconexión Automática | Manual | Nativa | Librería |
| Escalabilidad Horizontal | Excelente | Buena | Requiere Redis |
| Uso Recomendado | Consultas puntuales | Tracking de paquetes | Novedades críticas |

### Recomendación Final

1. **SSE** como estrategia principal para tracking de paquetes y actualización de estados.
2. **WebSocket** complementario para notificaciones de novedades críticas (alertas en tiempo real al operador).
3. **Polling** como fallback cuando SSE/WebSocket no estén disponibles.

---

## Consideraciones de Clean Code en el Cliente

1. **Separación de responsabilidades**: Cada hook encapsula un mecanismo de comunicación específico.
2. **Inversión de dependencias**: Los componentes consumen hooks, no implementaciones de red.
3. **Manejo de errores**: Todos los hooks exponen estado de error y reconexión automática.
4. **Configuración externa**: URLs y tiempos deben ser configurables vía variables de entorno.
5. **Desacoplamiento**: El hook `usePaqueteStream` no conoce la lógica de negocio; delega mediante callback `onEvento`.

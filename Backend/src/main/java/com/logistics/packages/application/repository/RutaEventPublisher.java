package com.logistics.packages.application.repository;

import java.util.UUID;

/**
 * @deprecated Este puerto está duplicado con com.logistics.packages.application.ports.RutaEventPublisher.
 * Se recomienda usar RutaQueuePort en su lugar, que es el patrón hexagonal correcto
 * para la comunicación asíncrona con el Módulo de Gestión de Rutas (M2).
 * 
 * MOD1-IP-003: Esta interfaz no se usa en el flujo de UC-003. Ver RutaEventAdapter para contexto.
 */
@Deprecated(since = "2026-05-21", forRemoval = true)
public interface RutaEventPublisher {
    /**
     * @deprecated Usar SolicitarRutaUseCase + RutaQueuePort en su lugar
     */
    @Deprecated(since = "2026-05-21", forRemoval = true)
    void publicarSolicitudRuta(UUID paqueteId);
}

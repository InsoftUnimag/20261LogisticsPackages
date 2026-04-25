package com.logistics.packages.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * T303 [P] - Evento de dominio que representa la solicitud de ruta de un paquete.
 * MOD1-IP-003 - Phase 1
 * 
 * Este evento se publica cuando un paquete ha sido admitido y pesado exitosamente,
 * señalizando que está listo para que se solicite su ruta al Módulo de Gestión de Rutas.
 */
@Getter
@AllArgsConstructor
public class SolicitudRutaEvent {
    private final UUID paqueteId;
    private final LocalDateTime timestamp;

    public static SolicitudRutaEvent of(UUID paqueteId) {
        return new SolicitudRutaEvent(paqueteId, LocalDateTime.now());
    }
}

package com.logistics.packages.application.usecase.novedad;

import com.logistics.packages.domain.valueobject.EstadoPaquete;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Respuesta del caso de uso de registro de novedad.
 * MOD1-UC-006: Devuelve información sobre la novedad registrada.
 */
@Getter
@AllArgsConstructor
public class RegistroNovedadResponse {
    
    /**
     * ID del paquete actualizado
     */
    private final UUID paqueteId;
    
    /**
     * Estado actual del paquete después de registrar la novedad
     */
    private final EstadoPaquete estadoActual;
    
    /**
     * ID del registro de historial generado
     */
    private final UUID historialId;
}

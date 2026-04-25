package com.logistics.packages.infrastructure.dto.response;

import com.logistics.packages.domain.valueobject.EstadoPaquete;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * DTO de respuesta para el registro de novedad.
 * MOD1-UC-006: Información retornada al cliente HTTP.
 */
@Getter
@AllArgsConstructor
public class RegistroNovedadResponseDto {
    
    private UUID paqueteId;
    private EstadoPaquete estadoActual;
    private UUID historialId;
    private String mensaje;
}

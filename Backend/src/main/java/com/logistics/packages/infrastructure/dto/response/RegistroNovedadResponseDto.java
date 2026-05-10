package com.logistics.packages.infrastructure.dto.response;

import com.logistics.packages.domain.valueobject.EstadoPaquete;
import io.swagger.v3.oas.annotations.media.Schema;
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
    
    @Schema(description = "ID del paquete con novedad")
    private UUID paqueteId;
    @Schema(description = "Estado actualizado del paquete tras la novedad")
    private EstadoPaquete estadoActual;
    @Schema(description = "ID del registro de historial generado")
    private UUID historialId;
    @Schema(description = "Mensaje informativo de la operación")
    private String mensaje;
}

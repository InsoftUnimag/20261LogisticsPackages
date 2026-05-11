package com.logistics.packages.infrastructure.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO para confirmar la clasificación de un paquete en una zona de destino.
 * MOD1-IP-005
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmarZonaRequest {
    
    @NotNull(message = "El ID del paquete es obligatorio")
    @Schema(description = "ID del paquete a clasificar")
    private UUID paqueteId;
    
    @NotNull(message = "El ID de la zona de destino es obligatorio")
    @Schema(description = "ID de la zona de destino confirmada")
    private UUID zonaDestinoId;
}

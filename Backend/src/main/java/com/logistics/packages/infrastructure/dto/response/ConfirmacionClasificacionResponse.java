package com.logistics.packages.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de respuesta tras confirmar la clasificación.
 * MOD1-IP-005
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmacionClasificacionResponse {
    @Schema(description = "ID del paquete clasificado")
    private UUID paqueteId;
    @Schema(description = "ID de la zona de destino confirmada")
    private UUID zonaDestinoId;
    @Schema(description = "Nombre de la zona de destino")
    private String nombreZona;
    @Schema(description = "Estado actual del paquete tras la clasificación")
    private String estadoPaquete;
    @Schema(description = "Mensaje informativo de la operación")
    private String mensaje;
}

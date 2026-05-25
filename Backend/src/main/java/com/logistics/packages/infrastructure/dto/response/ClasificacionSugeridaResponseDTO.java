package com.logistics.packages.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de respuesta con la zona de destino sugerida.
 * MOD1-IP-005
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClasificacionSugeridaResponseDTO {
    @Schema(description = "ID del paquete a clasificar")
    private UUID paqueteId;
    @Schema(description = "ID de la zona de destino sugerida")
    private UUID zonaDestinoId;
    @Schema(description = "Nombre de la zona de destino")
    private String nombreZona;
    @Schema(description = "Código de la zona de destino")
    private String codigoZona;
    @Schema(description = "Ciudad de destino del paquete")
    private String ciudadDestino;
    @Schema(description = "Tipo de mercancía del paquete (NORMAL, FRAGIL, PELIGROSO)")
    private String tipoMercancia;
    @Schema(description = "Indica si la zona tiene capacidad disponible")
    private boolean tieneCapacidad;
    @Schema(description = "Mensaje informativo de la sugerencia")
    private String mensaje;
}

package com.logistics.packages.infrastructure.dto.response;

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
    private UUID paqueteId;
    private UUID zonaDestinoId;
    private String nombreZona;
    private String codigoZona;
    private boolean tieneCapacidad;
    private String mensaje;
}

package com.logistics.packages.infrastructure.dto.response;

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
    private UUID paqueteId;
    private UUID zonaDestinoId;
    private String nombreZona;
    private String estadoPaquete;
    private String mensaje;
}

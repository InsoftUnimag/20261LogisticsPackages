package com.logistics.packages.application.usecase;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Respuesta con la zona de destino sugerida para un paquete.
 * MOD1-IP-005
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClasificacionSugeridaResponse {
    private UUID paqueteId;
    private UUID zonaDestinoId;
    private String nombreZona;
    private String codigoZona;
}

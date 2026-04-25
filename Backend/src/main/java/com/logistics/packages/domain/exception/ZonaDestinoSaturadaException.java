package com.logistics.packages.domain.exception;

import lombok.Getter;

import java.util.UUID;

/**
 * Excepción lanzada cuando una zona de destino ha alcanzado su capacidad máxima.
 * MOD1-IP-005: FR-004
 */
@Getter
public class ZonaDestinoSaturadaException extends RuntimeException {
    
    private final UUID zonaId;
    private final String nombreZona;
    private final UUID zonaDesbordeId;
    
    public ZonaDestinoSaturadaException(UUID zonaId, String nombreZona, UUID zonaDesbordeId) {
        super(String.format(
            "La zona de destino %s (ID: %s) ha alcanzado su capacidad máxima. " +
            "Se sugiere usar la zona de desborde con ID: %s",
            nombreZona, zonaId, zonaDesbordeId
        ));
        this.zonaId = zonaId;
        this.nombreZona = nombreZona;
        this.zonaDesbordeId = zonaDesbordeId;
    }
}

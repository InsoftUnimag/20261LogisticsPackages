package com.logistics.packages.domain.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta registrar una novedad de tipo DAÑADO
 * sin proporcionar la evidencia fotográfica obligatoria.
 * MOD1-UC-006: FR-004 - Requerir evidencia fotográfica obligatoria para cambios a DAÑADO.
 */
public class EvidenciaRequeridaException extends RuntimeException {
    
    private final UUID paqueteId;
    
    public EvidenciaRequeridaException(UUID paqueteId) {
        super(String.format(
            "La evidencia fotográfica es obligatoria para registrar una novedad de tipo DAÑADO " +
            "en el paquete %s. Debe adjuntar al menos un archivo multimedia válido.",
            paqueteId
        ));
        this.paqueteId = paqueteId;
    }
    
    public UUID getPaqueteId() {
        return paqueteId;
    }
}

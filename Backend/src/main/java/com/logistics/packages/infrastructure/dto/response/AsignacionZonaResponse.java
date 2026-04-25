package com.logistics.packages.infrastructure.dto.response;

import com.logistics.packages.domain.valueobject.EstadoPaquete;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de respuesta para la asignación de zona de almacenamiento.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionZonaResponse {
    
    private UUID paqueteId;
    private UUID zonaId;
    private String nombreZona;
    private EstadoPaquete estadoPaquete;
    private boolean datosActualizados;
    private String mensaje;
    
    // Información de zona de contingencia si aplica
    private UUID zonaContingenciaId;
    private boolean usandoZonaContingencia;
}

package com.logistics.packages.infrastructure.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para la solicitud de asignación de zona de almacenamiento a un paquete.
 * FR-001: Asignación por UUID del paquete
 * FR-004: Soporte para actualización de datos físicos por discrepancia
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignarZonaRequest {
    
    @NotNull(message = "El ID del paquete es obligatorio")
    private UUID paqueteId;
    
    @NotNull(message = "El ID de la zona es obligatorio")
    private UUID zonaId;
    
    // FR-004: Campos opcionales para actualización de datos físicos por discrepancia
    private DatosFisicosDiscrepanciaDto datosDiscrepancia;
    
    /**
     * Indica si hay discrepancias físicas que requieren actualización.
     */
    public boolean tieneDiscrepancias() {
        return datosDiscrepancia != null;
    }
}

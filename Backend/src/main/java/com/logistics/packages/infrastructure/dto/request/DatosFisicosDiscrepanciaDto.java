package com.logistics.packages.infrastructure.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para datos físicos del paquete cuando se detectan discrepancias.
 * FR-004: Actualización directa de datos por discrepancia física
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosFisicosDiscrepanciaDto {
    
    @NotNull(message = "El peso es obligatorio cuando hay discrepancia")
    @Positive(message = "El peso debe ser mayor a cero")
    @Schema(description = "Peso corregido en kg")
    private Double pesoKg;
    
    @NotNull(message = "El largo es obligatorio cuando hay discrepancia")
    @Positive(message = "El largo debe ser mayor a cero")
    @Schema(description = "Largo corregido en cm")
    private Double largoCm;
    
    @NotNull(message = "El ancho es obligatorio cuando hay discrepancia")
    @Positive(message = "El ancho debe ser mayor a cero")
    @Schema(description = "Ancho corregido en cm")
    private Double anchoCm;
    
    @NotNull(message = "El alto es obligatorio cuando hay discrepancia")
    @Positive(message = "El alto debe ser mayor a cero")
    @Schema(description = "Alto corregido en cm")
    private Double altoCm;
    
    @Schema(description = "Observaciones sobre la discrepancia encontrada")
    private String observaciones;
}

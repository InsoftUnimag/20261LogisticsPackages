package com.logistics.packages.infrastructure.dto.request;

import com.logistics.packages.domain.valueobject.TipoMercancia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de request para el procesamiento de pesaje (MOD1-UC-002)
 * T209: Incluye validaciones para FR-001 y FR-003
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PesajeRequest {
    
    @NotNull(message = "El ID del paquete es requerido")
    @Schema(description = "ID del paquete a procesar")
    private UUID paqueteId;
    
    @NotNull(message = "El peso es requerido")
    @DecimalMin(value = "0.01", message = "El peso debe ser mayor a 0")
    @DecimalMax(value = "70.0", message = "El peso no puede exceder los 70 kg")
    @Schema(description = "Peso real del paquete en kg (0.01 - 70.0)")
    private Double peso;
    
    @NotNull(message = "El largo es requerido")
    @DecimalMin(value = "0.01", message = "El largo debe ser mayor a 0")
    @Schema(description = "Largo del paquete en cm")
    private Double largoCm;
    
    @NotNull(message = "El ancho es requerido")
    @DecimalMin(value = "0.01", message = "El ancho debe ser mayor a 0")
    @Schema(description = "Ancho del paquete en cm")
    private Double anchoCm;
    
    @NotNull(message = "El alto es requerido")
    @DecimalMin(value = "0.01", message = "El alto debe ser mayor a 0")
    @Schema(description = "Alto del paquete en cm")
    private Double altoCm;
    
    @NotNull(message = "El tipo de mercancía es requerido")
    @Schema(description = "Tipo de mercancía (ESTANDAR | FRAGIL | PELIGROSO)")
    private TipoMercancia tipoMercancia;
    
    @NotNull(message = "El indicador de forma irregular es requerido")
    @Schema(description = "Indica si el paquete tiene forma irregular")
    private Boolean formaIrregular;
}

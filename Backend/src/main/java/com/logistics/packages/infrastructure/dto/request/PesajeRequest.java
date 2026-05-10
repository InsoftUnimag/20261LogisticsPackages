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
    
    @NotNull(message = "La tarifa base es requerida")
    @DecimalMin(value = "0.0", message = "La tarifa base debe ser mayor o igual a 0")
    @Schema(description = "Tarifa base para el cálculo del precio")
    private BigDecimal tarifaBase;
    
    @NotNull(message = "La tarifa por kg es requerida")
    @DecimalMin(value = "0.0", message = "La tarifa por kg debe ser mayor o igual a 0")
    @Schema(description = "Tarifa por kilogramo para el cálculo del precio")
    private BigDecimal tarifaPorKg;
    
    @NotNull(message = "La tarifa por km es requerida")
    @DecimalMin(value = "0.0", message = "La tarifa por km debe ser mayor o igual a 0")
    @Schema(description = "Tarifa por kilómetro para el cálculo del precio")
    private BigDecimal tarifaPorKm;
    
    @NotNull(message = "El recargo por tipo de mercancía es requerido")
    @DecimalMin(value = "0.0", message = "El recargo por tipo de mercancía debe ser mayor o igual a 0")
    @Schema(description = "Recargo aplicado según el tipo de mercancía")
    private BigDecimal recargoTipoMercancia;
    
    @NotNull(message = "El recargo por categoría de carga es requerido")
    @DecimalMin(value = "0.0", message = "El recargo por categoría de carga debe ser mayor o igual a 0")
    @Schema(description = "Recargo aplicado según la categoría de carga")
    private BigDecimal recargoCategoriaCarga;
}

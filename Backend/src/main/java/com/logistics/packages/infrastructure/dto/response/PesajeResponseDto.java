package com.logistics.packages.infrastructure.dto.response;

import com.logistics.packages.domain.valueobject.CategoriaCarga;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para el procesamiento de pesaje (MOD1-UC-002)
 * Incluye los resultados de los cálculos y las alertas generadas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PesajeResponseDto {
    
    @Schema(description = "ID del paquete pesado")
    private UUID paqueteId;
    @Schema(description = "Peso real del paquete en kg")
    private Double peso;
    @Schema(description = "Volumen calculado del paquete en m³")
    private Double volumenM3;
    @Schema(description = "Peso volumétrico calculado (para comparar con peso real)")
    private Double pesoVolumetrico;
    @Schema(description = "Peso facturable final (máximo entre peso real y volumétrico)")
    private Double pesoFacturable;
    @Schema(description = "Categoría de carga (NORMAL | CARGA_ESPECIAL)")
    private CategoriaCarga categoriaCarga;
    @Schema(description = "Precio total calculado del envío")
    private BigDecimal precioEnvio;
    
    @Builder.Default
    @Schema(description = "Lista de alertas generadas durante el proceso (carga especial, densidad atípica)")
    private List<String> alertas = new ArrayList<>();
    
    /**
     * Método helper para agregar alertas basadas en los flags del dominio
     */
    public void agregarAlertaSiAplica(boolean alertaCargaEspecial, boolean alertaDensidadAtipica) {
        if (alertaCargaEspecial) {
            alertas.add("CARGA_ESPECIAL: El paquete requiere manejo especial por peso o volumen");
        }
        if (alertaDensidadAtipica) {
            alertas.add("DENSIDAD_ATIPICA: Verificar el peso y dimensiones del paquete");
        }
    }
}

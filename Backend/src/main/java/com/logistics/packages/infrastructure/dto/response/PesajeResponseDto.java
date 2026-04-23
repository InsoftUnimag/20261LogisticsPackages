package com.logistics.packages.infrastructure.dto.response;

import com.logistics.packages.domain.valueobject.CategoriaCarga;
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
    
    private UUID paqueteId;
    private Double peso;
    private Double volumenM3;
    private Double pesoVolumetrico;
    private Double pesoFacturable;
    private CategoriaCarga categoriaCarga;
    private BigDecimal precioEnvio;
    
    @Builder.Default
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

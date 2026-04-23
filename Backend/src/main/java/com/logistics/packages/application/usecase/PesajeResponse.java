package com.logistics.packages.application.usecase;

import com.logistics.packages.domain.valueobject.CategoriaCarga;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Respuesta del proceso de pesaje de un paquete (MOD1-UC-002)
 * Contiene los datos calculados y las alertas generadas.
 */
@Getter
@Builder
@AllArgsConstructor
public class PesajeResponse {
    private UUID paqueteId;
    private Double peso;
    private Double volumenM3;
    private Double pesoVolumetrico;
    private Double pesoFacturable;
    private CategoriaCarga categoriaCarga;
    private BigDecimal precioEnvio;
    private boolean alertaCargaEspecial;
    private boolean alertaDensidadAtipica;
}

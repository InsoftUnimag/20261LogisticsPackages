package com.logistics.packages.application.usecase;

import com.logistics.packages.domain.valueobject.Dimensiones;
import com.logistics.packages.domain.valueobject.Peso;
import com.logistics.packages.domain.valueobject.TipoMercancia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Comando para procesar el pesaje de un paquete (MOD1-UC-002)
 * Encapsula todos los datos necesarios para el proceso de pesaje.
 */
@Getter
@Builder
@AllArgsConstructor
public class PesajeCommand {
    private final UUID paqueteId;
    private final Peso peso;
    private final Dimensiones dimensiones;
    private final TipoMercancia tipoMercancia;
    private final boolean formaIrregular;
    
    // Tarifas para el cálculo del precio
    private final BigDecimal tarifaBase;
    private final BigDecimal tarifaPorKg;
    private final BigDecimal tarifaPorKm;
    private final BigDecimal recargoTipoMercancia;
    private final BigDecimal recargoCategoriaCarga;

    // Evidencia fotográfica opcional (MOD1-UC-002 FR-004)
    private final MultipartFile evidencia;
}

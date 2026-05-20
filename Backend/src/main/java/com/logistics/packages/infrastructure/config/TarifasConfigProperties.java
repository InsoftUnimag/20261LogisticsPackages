package com.logistics.packages.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Propiedades de configuración de tarifas.
 * Se cargan desde application.yml bajo la clave app.tarifas.*
 * 
 * BE-3: Las tarifas se leen desde la configuración del servidor, no desde el cliente.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.tarifas")
public class TarifasConfigProperties {
    
    private BigDecimal base = new BigDecimal("5000");
    private BigDecimal porKg = new BigDecimal("250");
    private BigDecimal porKm = new BigDecimal("150");
    private BigDecimal recargoFragil = new BigDecimal("1000");
    private BigDecimal recargoPeligroso = new BigDecimal("2000");
    private BigDecimal recargoCargaEspecial = new BigDecimal("500");
}

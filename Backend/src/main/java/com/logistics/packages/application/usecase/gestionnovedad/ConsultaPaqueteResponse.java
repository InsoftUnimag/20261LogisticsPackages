package com.logistics.packages.application.usecase.gestionnovedad;

import com.logistics.packages.domain.model.HistorialEstado;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para consultas del Módulo de Gestión de Finanzas.
 * MOD1-UC-007: FR-005, FR-007 - Endpoint de consulta síncrona.
 * 
 * Este DTO proporciona toda la información necesaria para que el Módulo de Finanzas
 * pueda realizar ajustes financieros basados en el estado del paquete.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaPaqueteResponse {
    
    /**
     * ID de la ruta
     */
    private UUID idRoute;
    
    /**
     * ID del paquete
     */
    private UUID idPaquete;
    
    /**
     * Estado actual del paquete
     */
    private String estado;
    
    /**
     * Valor declarado del paquete
     */
    private BigDecimal valorDeclarado;
    
    /**
     * Precio del envío calculado
     */
    private BigDecimal precioEnvio;
    
    /**
     * Método de pago utilizado
     */
    private String metodoPago;
    
    /**
     * Fecha de ingreso del paquete (UTC)
     */
    private LocalDateTime fechaIngresoUtc;
    
    /**
     * Fecha de entrega del paquete (UTC), si aplica
     */
    private LocalDateTime fechaEntregaUtc;
    
    /**
     * URL de la evidencia de entrega (POD)
     */
    private String urlEvidenciaEntrega;
    
    /**
     * Nombre del firmante que recibió el paquete
     */
    private String nombreFirmante;
    
    /**
     * Historial completo de transiciones de estado
     */
    private List<HistorialEstado> historialEstados;
}

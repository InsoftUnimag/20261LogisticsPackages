package com.logistics.packages.application.usecase.gestionnovedad;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para representar un evento proveniente del Módulo de Gestión de Rutas.
 * MOD1-UC-007: FR-001 - Recibir y procesar eventos del Módulo 2.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoRutaDto {
    
    /**
     * ID único del evento (para garantizar idempotencia)
     */
    private String eventoId;
    
    /**
     * ID del paquete afectado
     */
    private UUID paqueteId;
    
    /**
     * ID de la ruta asociada
     */
    private UUID rutaId;
    
    /**
     * Tipo de evento (EN_TRANSITO, EN_PARADA_DE_ENTREGA, ENTREGADO, etc.)
     */
    private TipoEventoRuta tipoEvento;
    
    /**
     * Observaciones o notas sobre el evento
     */
    private String observaciones;
    
    /**
     * URL de evidencia (obligatoria para ENTREGADO y DAÑADO)
     */
    private String urlEvidencia;
    
    /**
     * Nombre del firmante (para eventos de tipo ENTREGADO)
     */
    private String nombreFirmante;
    
    /**
     * Motivo (para eventos de tipo DEVOLUCION)
     */
    private String motivo;
    
    /**
     * Enum para tipos de eventos de ruta
     */
    public enum TipoEventoRuta {
        EN_TRANSITO,
        EN_PARADA_DE_ENTREGA,
        ENTREGADO,
        DEVOLUCION,
        EXTRAVIADO,
        DAÑADO
    }
}

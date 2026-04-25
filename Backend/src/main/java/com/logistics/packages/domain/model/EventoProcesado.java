package com.logistics.packages.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Entidad que registra los eventos ya procesados para garantizar idempotencia.
 * MOD1-UC-007: FR-008 - Detectar y prevenir el procesamiento de eventos duplicados.
 * 
 * Esta entidad actúa como un registro de auditoría de eventos procesados,
 * permitiendo verificar si un mensaje del Módulo 2 ya fue procesado anteriormente.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "eventoId")
public class EventoProcesado {
    
    /**
     * ID único del evento (proveniente del mensaje del Módulo 2)
     */
    private String eventoId;
    
    /**
     * ID del paquete asociado al evento
     */
    private UUID paqueteId;
    
    /**
     * Tipo de evento procesado (ej: "PAQUETE_EN_TRANSITO", "PAQUETE_ENTREGADO")
     */
    private String tipoEvento;
    
    /**
     * Timestamp UTC del momento en que se procesó el evento
     */
    @Builder.Default
    private LocalDateTime fechaProcesamientoUtc = LocalDateTime.now(ZoneOffset.UTC);
    
    /**
     * Constructor de dominio para crear un nuevo registro de evento procesado.
     * 
     * @param eventoId ID único del evento
     * @param paqueteId ID del paquete asociado
     * @param tipoEvento Tipo de evento
     */
    public EventoProcesado(String eventoId, UUID paqueteId, String tipoEvento) {
        this.eventoId = eventoId;
        this.paqueteId = paqueteId;
        this.tipoEvento = tipoEvento;
        this.fechaProcesamientoUtc = LocalDateTime.now(ZoneOffset.UTC);
    }
}

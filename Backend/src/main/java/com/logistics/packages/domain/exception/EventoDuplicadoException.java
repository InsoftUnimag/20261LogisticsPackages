package com.logistics.packages.domain.exception;

/**
 * Excepción lanzada cuando se intenta procesar un evento que ya fue procesado anteriormente.
 * MOD1-UC-007: FR-008 - Detectar y prevenir el procesamiento de eventos duplicados.
 * 
 * Esta excepción es parte de la estrategia de idempotencia que garantiza que un mensaje
 * del Módulo 2 no se procese más de una vez, evitando inconsistencias en el historial.
 */
public class EventoDuplicadoException extends RuntimeException {
    
    private final String eventoId;
    
    public EventoDuplicadoException(String eventoId) {
        super(String.format("El evento con ID '%s' ya fue procesado anteriormente.", eventoId));
        this.eventoId = eventoId;
    }
    
    public String getEventoId() {
        return eventoId;
    }
}

package com.logistics.packages.application.ports;

import com.logistics.packages.domain.model.EventoProcesado;

import java.util.Optional;

/**
 * Puerto de salida para la persistencia de eventos procesados.
 * MOD1-UC-007: FR-008 - Garantiza la idempotencia en el procesamiento de eventos.
 * 
 * Este puerto sigue el principio de Inversión de Dependencias (SOLID),
 * permitiendo que la capa de aplicación no dependa de implementaciones concretas.
 */
public interface EventoProcesadoRepository {
    
    /**
     * Guarda un registro de evento procesado.
     * 
     * @param eventoProcesado El evento a guardar
     * @return El evento guardado
     */
    EventoProcesado guardar(EventoProcesado eventoProcesado);
    
    /**
     * Verifica si un evento ya fue procesado anteriormente.
     * 
     * @param eventoId ID único del evento
     * @return true si el evento ya fue procesado, false en caso contrario
     */
    boolean yaFueProcesado(String eventoId);
    
    /**
     * Busca un evento procesado por su ID.
     * 
     * @param eventoId ID único del evento
     * @return Optional con el evento si existe, empty en caso contrario
     */
    Optional<EventoProcesado> buscarPorEventoId(String eventoId);
}

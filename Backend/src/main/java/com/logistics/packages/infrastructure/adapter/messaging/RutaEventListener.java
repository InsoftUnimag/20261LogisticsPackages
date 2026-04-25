package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.usecase.gestionnovedad.EventoRutaDto;
import com.logistics.packages.application.usecase.gestionnovedad.ProcesarEventoRutaUseCase;
import com.logistics.packages.domain.exception.EventoDuplicadoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener para eventos de ruta provenientes del Módulo de Gestión de Rutas.
 * MOD1-UC-007: FR-001, FR-004 - Procesar eventos asíncronos desde el Módulo 2.
 * 
 * Este adaptador escucha la cola de eventos de ruta y delega el procesamiento
 * al caso de uso correspondiente.
 * 
 * Principio aplicado: Dependency Inversion (depende de abstracción, no de implementación)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RutaEventListener {
    
    private final ProcesarEventoRutaUseCase procesarEventoRutaUseCase;
    
    /**
     * Escucha y procesa eventos de ruta del Módulo 2.
     * 
     * La cola debe ser configurada en application.properties con el nombre:
     * app.messaging.ruta.eventos.queue
     * 
     * @param eventoDto DTO con la información del evento
     */
    @RabbitListener(queues = "${app.messaging.ruta.eventos.queue:eventos_ruta_queue}")
    public void onEventoRuta(EventoRutaDto eventoDto) {
        log.info("Evento de ruta recibido: {} para paquete: {}", 
                eventoDto.getEventoId(), eventoDto.getPaqueteId());
        
        try {
            // Delegar al caso de uso para el procesamiento
            procesarEventoRutaUseCase.procesar(eventoDto);
            
            log.info("Evento de ruta procesado exitosamente: {}", eventoDto.getEventoId());
            
        } catch (EventoDuplicadoException e) {
            // FR-008: El evento duplicado ya fue manejado, no es un error crítico
            log.warn("Evento duplicado detectado y descartado: {}", e.getEventoId());
            // No relanzar la excepción para evitar que el mensaje vuelva a la cola
            
        } catch (Exception e) {
            log.error("Error al procesar evento de ruta {}: {}", 
                    eventoDto.getEventoId(), e.getMessage(), e);
            // Relanzar para que el mensaje vaya a la Dead Letter Queue
            throw new RuntimeException("Error al procesar evento de ruta", e);
        }
    }
}

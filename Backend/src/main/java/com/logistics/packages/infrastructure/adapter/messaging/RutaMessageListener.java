package com.logistics.packages.infrastructure.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.packages.application.usecase.AsignarRutaUseCase;
import com.logistics.packages.infrastructure.dto.response.RespuestaRutaPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * T311 [P] [US3] - Listener para respuestas de asignación de ruta
 * MOD1-IP-003 - Phase 3
 * 
 * Escucha mensajes en la cola de respuestas del Módulo de Gestión de Rutas
 * y procesa las asignaciones de ruta.
 * FR-003: Recibe el ID de ruta y lo almacena en el paquete
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RutaMessageListener {

    private final AsignarRutaUseCase asignarRutaUseCase;
    private final ObjectMapper objectMapper;

    /**
     * Procesa mensajes de respuesta de asignación de ruta
     * 
     * @param message El mensaje JSON recibido de la cola
     */
    @RabbitListener(queues = "${app.messaging.ruta.response.queue:respuestas_ruta_queue}")
    public void recibirRespuesta(String message) {
        log.info("Mensaje recibido de la cola de respuestas de ruta: {}", message);
        
        try {
            // Deserializar el mensaje JSON
            RespuestaRutaPayload payload = objectMapper.readValue(message, RespuestaRutaPayload.class);
            
            log.info("Procesando respuesta de ruta para paquete: {} - Estado: {}",
                    payload.getPaqueteId(), payload.getEstado());
            
            // Delegar al caso de uso para asignar la ruta
            asignarRutaUseCase.asignarRuta(payload);
            
        } catch (JsonProcessingException e) {
            log.error("Error deserializando respuesta de ruta: {}", e.getMessage(), e);
            // En un sistema real, esto podría enviarse a una DLQ (Dead Letter Queue)
            throw new RuntimeException("Error al deserializar respuesta de ruta", e);
        } catch (Exception e) {
            log.error("Error procesando respuesta de ruta: {}", e.getMessage(), e);
            // En caso de error, el mensaje puede ser reencolado según la configuración
            throw e;
        }
    }
}

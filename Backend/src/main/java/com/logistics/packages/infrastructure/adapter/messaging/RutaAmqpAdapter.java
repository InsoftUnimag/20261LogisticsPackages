package com.logistics.packages.infrastructure.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.packages.application.ports.RutaQueuePort;
import com.logistics.packages.infrastructure.dto.request.SolicitudRutaPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * T310 [P] [US3] - Adaptador AMQP para enviar solicitudes de ruta
 * MOD1-IP-003 - Phase 3
 * 
 * Implementación del puerto RutaQueuePort usando RabbitMQ.
 * FR-001: Comunicación estrictamente asíncrona mediante payloads JSON
 * FR-004: Registra cada intento de envío
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RutaAmqpAdapter implements RutaQueuePort {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final String EXCHANGE = "solicitudes_ruta_exchange";
    private static final String ROUTING_KEY = "solicitud.nueva";

    /**
     * Envía una solicitud de ruta al Módulo de Gestión de Rutas de forma asíncrona
     * 
     * @param payload El payload con los datos del paquete
     */
    @Override
    public void enviarSolicitud(SolicitudRutaPayload payload) {
        try {
            // FR-004: Registrar intento
            log.info("Enviando solicitud de ruta para paquete: {}", payload.getPaqueteId());
            
            // Serializar el payload a JSON
            String jsonPayload = objectMapper.writeValueAsString(payload);
            
            // Enviar a la cola de forma asíncrona
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, jsonPayload);
            
            log.info("Solicitud de ruta enviada exitosamente a la cola: {}", EXCHANGE);
            
        } catch (JsonProcessingException e) {
            log.error("Error serializando payload de solicitud de ruta para paquete {}: {}",
                    payload.getPaqueteId(), e.getMessage(), e);
            throw new RuntimeException("Error al serializar solicitud de ruta", e);
        } catch (Exception e) {
            log.error("Error enviando solicitud de ruta para paquete {}: {}",
                    payload.getPaqueteId(), e.getMessage(), e);
            // FR-005: En caso de error, el mensaje se puede encolar para reintento
            throw new RuntimeException("Error al enviar solicitud de ruta", e);
        }
    }
}

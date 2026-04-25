package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.repository.NovedadEventPublisher;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptador para publicar eventos de novedades usando AWS SQS.
 * MOD1-UC-006: FR-005 - Implementación del puerto NovedadEventPublisher.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NovedadEventAdapter implements NovedadEventPublisher {
    
    private final SqsTemplate sqsTemplate;
    
    @Value("${aws.sqs.novedad-queue:novedad-registrada-queue}")
    private String queueName;

    @Override
    public void publicarNovedadRegistrada(UUID paqueteId, UUID historialId) {
        try {
            // Construir el payload del evento
            Map<String, Object> evento = new HashMap<>();
            evento.put("eventType", "NOVEDAD_REGISTRADA");
            evento.put("paqueteId", paqueteId.toString());
            evento.put("historialId", historialId.toString());
            evento.put("timestamp", System.currentTimeMillis());
            
            // Publicar el evento en la cola SQS
            sqsTemplate.send(to -> to.queue(queueName).payload(evento));
            
            log.info("Evento de novedad publicado exitosamente. PaqueteId: {}, HistorialId: {}", 
                paqueteId, historialId);
            
        } catch (Exception e) {
            log.error("Error al publicar evento de novedad para el paquete {}: {}", 
                paqueteId, e.getMessage());
            // No lanzamos la excepción para no revertir la transacción principal
            // El sistema de mensajería debería tener retry automático
        }
    }
}

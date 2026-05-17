package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.repository.RutaEventPublisher;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RutaEventAdapter implements RutaEventPublisher {

    private final SqsTemplate sqsTemplate;

    @Value("${aws.sqs.ruta-event-queue:solicitudes-ruta-event-queue}")
    private String queueName;

    @Override
    public void publicarSolicitudRuta(UUID paqueteId) {
        try {
            Map<String, Object> evento = new HashMap<>();
            evento.put("eventType", "SOLICITUD_RUTA");
            evento.put("paqueteId", paqueteId.toString());
            evento.put("timestamp", System.currentTimeMillis());

            sqsTemplate.send(to -> to.queue(queueName).payload(evento));

            log.info("Evento de solicitud de ruta publicado exitosamente. PaqueteId: {}", paqueteId);
        } catch (Exception e) {
            log.error("Error al publicar evento de solicitud de ruta para el paquete {}: {}",
                    paqueteId, e.getMessage());
        }
    }
}

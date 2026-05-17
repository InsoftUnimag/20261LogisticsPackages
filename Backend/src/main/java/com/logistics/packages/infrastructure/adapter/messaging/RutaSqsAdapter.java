package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.ports.RutaQueuePort;
import com.logistics.packages.infrastructure.dto.request.SolicitudRutaPayload;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RutaSqsAdapter implements RutaQueuePort {

    private final SqsTemplate sqsTemplate;

    @Value("${aws.sqs.ruta-request-queue:solicitudes-ruta-queue}")
    private String queueName;

    @Override
    public void enviarSolicitud(SolicitudRutaPayload payload) {
        try {
            log.info("Enviando solicitud de ruta para paquete: {}", payload.getPaqueteId());
            sqsTemplate.send(to -> to.queue(queueName).payload(payload));
            log.info("Solicitud de ruta enviada exitosamente a SQS: {}", queueName);
        } catch (Exception e) {
            log.error("Error enviando solicitud de ruta para paquete {}: {}",
                    payload.getPaqueteId(), e.getMessage(), e);
            throw new RuntimeException("Error al enviar solicitud de ruta", e);
        }
    }
}

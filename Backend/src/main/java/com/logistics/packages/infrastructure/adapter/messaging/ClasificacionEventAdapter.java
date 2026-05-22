package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.ports.ClasificacionEventPublisher;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptador SQS para publicar eventos de clasificación.
 * MOD1-IP-004: FR-009 - Publica el evento paquete_listo_para_clasificar
 * que es consumido por el listener de UC-005.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClasificacionEventAdapter implements ClasificacionEventPublisher {

    private final SqsTemplate sqsTemplate;

    @Value("${aws.sqs.clasificacion-queue:paquete-listo-clasificar-queue}")
    private String queueName;

    @Override
    public void publicarPaqueteListoParaClasificar(UUID paqueteId) {
        try {
            Map<String, Object> evento = new HashMap<>();
            evento.put("eventType", "PAQUETE_LISTO_PARA_CLASIFICAR");
            evento.put("paqueteId", paqueteId.toString());
            evento.put("timestamp", System.currentTimeMillis());

            sqsTemplate.send(to -> to.queue(queueName).payload(evento));

            log.info("Evento de clasificación publicado exitosamente. PaqueteId: {}", paqueteId);
        } catch (Exception e) {
            log.error("Error al publicar evento de clasificación para el paquete {}: {}",
                    paqueteId, e.getMessage(), e);
        }
    }
}

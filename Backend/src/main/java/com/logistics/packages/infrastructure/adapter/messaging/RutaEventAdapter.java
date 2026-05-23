package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.repository.RutaEventPublisher;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @deprecated Este adaptador implementa un patrón de eventos obsoleto.
 * MOD1-IP-003: El flujo correcto para solicitar rutas es:
 * 1. ProcesarPesajeUseCase dispara SolicitudRutaEvent
 * 2. SolicitarRutaUseCase maneja el evento
 * 3. RutaSqsAdapter (implementa RutaQueuePort) envía a SQS
 * 
 * Este adaptador NO participa en el flujo de UC-003 y publica un payload
 * camelCase que no cumple el contrato snake_case de M2.
 */
@Deprecated(since = "2026-05-21", forRemoval = true)
@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class RutaEventAdapter implements RutaEventPublisher {

    private final SqsTemplate sqsTemplate;

    @Value("${aws.sqs.ruta-event-queue:solicitudes-ruta-event-queue}")
    private String queueName;

    @Override
    @Deprecated(since = "2026-05-21", forRemoval = true)
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

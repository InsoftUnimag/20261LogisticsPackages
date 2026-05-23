package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.ports.EstadoPaqueteFinanzasPublisher;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.infrastructure.dto.event.EventoFinancieroPaqueteDto;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinanzasEventSqsAdapter implements EstadoPaqueteFinanzasPublisher {

    private final SqsTemplate sqsTemplate;

    @Value("${app.sqs.eventos-financieros-queue:eventos-financieros-paquete-queue}")
    private String queueName;

    @Override
    public void publicarEstadoFinal(Paquete paquete) {
        try {
            EventoFinancieroPaqueteDto evento = mapToEvento(paquete);

            log.info("Publicando estado a cola de finanzas: {} para paquete: {}",
                    paquete.getEstado(), paquete.getId());

            sqsTemplate.send(to -> to.queue(queueName).payload(evento));

            log.info("Estado publicado exitosamente a SQS: {} - Paquete: {}",
                    queueName, paquete.getId());
        } catch (Exception e) {
            log.error("Error al publicar estado a cola de finanzas para paquete {}: {}",
                    paquete.getId(), e.getMessage(), e);
        }
    }

    private EventoFinancieroPaqueteDto mapToEvento(Paquete paquete) {
        return EventoFinancieroPaqueteDto.builder()
                .idPaquete(paquete.getId())
                .idRuta(paquete.getRutaId())
                .estado(paquete.getEstado().name())
                .build();
    }
}

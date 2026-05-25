package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.usecase.AsignarRutaCommand;
import com.logistics.packages.application.usecase.AsignarRutaUseCase;
import com.logistics.packages.infrastructure.dto.response.RespuestaRutaPayload;
import com.logistics.packages.infrastructure.messaging.consumers.JsonNodeMapper;
import com.fasterxml.jackson.databind.JsonNode;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"default", "local", "aws"})
@RequiredArgsConstructor
public class RutaSqsListener {

    private static final String TIPO_EVENTO_RUTA_ASIGNADA = "RUTA_ASIGNADA";

    private final AsignarRutaUseCase asignarRutaUseCase;

    @SqsListener("${aws.sqs.ruta-response-queue:respuestas-ruta-queue}")
    public void recibirRespuesta(Object payload) {
        // Convertir el payload a JsonNode para manejo seguro
        JsonNode jsonNode = JsonNodeMapper.toJsonNode(payload);
        
        // Deserializar al DTO local de M1
        RespuestaRutaPayload rutaPayload = JsonNodeMapper.fromJsonNode(jsonNode, RespuestaRutaPayload.class);
        
        log.info("Mensaje recibido de la cola de respuestas de ruta: {} - tipo_evento: {}",
                rutaPayload.getPaqueteId(), rutaPayload.getTipoEvento());

        if (!TIPO_EVENTO_RUTA_ASIGNADA.equals(rutaPayload.getTipoEvento())) {
            log.warn("Tipo de evento inesperado: {}. Se descarta el mensaje.", rutaPayload.getTipoEvento());
            return;
        }

        if (rutaPayload.getRutaId() == null) {
            log.warn("Mensaje RUTA_ASIGNADA sin ruta_id para paquete {}. Se descarta.", rutaPayload.getPaqueteId());
            return;
        }

        AsignarRutaCommand command = new AsignarRutaCommand(
                rutaPayload.getPaqueteId(),
                rutaPayload.getRutaId(),
                rutaPayload.getFechaHoraEvento()
        );

        asignarRutaUseCase.asignarRuta(command);
    }
}

package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.usecase.AsignarRutaCommand;
import com.logistics.packages.application.usecase.AsignarRutaUseCase;
import com.logistics.packages.infrastructure.dto.response.RespuestaRutaPayload;
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
    public void recibirRespuesta(RespuestaRutaPayload payload) {
        log.info("Mensaje recibido de la cola de respuestas de ruta: {} - tipo_evento: {}",
                payload.getPaqueteId(), payload.getTipoEvento());

        if (!TIPO_EVENTO_RUTA_ASIGNADA.equals(payload.getTipoEvento())) {
            log.warn("Tipo de evento inesperado: {}. Se descarta el mensaje.", payload.getTipoEvento());
            return;
        }

        if (payload.getRutaId() == null) {
            log.warn("Mensaje RUTA_ASIGNADA sin ruta_id para paquete {}. Se descarta.", payload.getPaqueteId());
            return;
        }

        AsignarRutaCommand command = new AsignarRutaCommand(
                payload.getPaqueteId(),
                payload.getRutaId(),
                payload.getFechaHoraEvento()
        );

        asignarRutaUseCase.asignarRuta(command);
    }
}

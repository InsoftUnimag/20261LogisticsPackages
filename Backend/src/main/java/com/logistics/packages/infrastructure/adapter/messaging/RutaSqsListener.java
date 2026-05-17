package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.usecase.AsignarRutaUseCase;
import com.logistics.packages.infrastructure.dto.response.RespuestaRutaPayload;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RutaSqsListener {

    private final AsignarRutaUseCase asignarRutaUseCase;

    @SqsListener("${aws.sqs.ruta-response-queue:respuestas-ruta-queue}")
    public void recibirRespuesta(RespuestaRutaPayload payload) {
        log.info("Mensaje recibido de la cola de respuestas de ruta: {} - Estado: {}",
                payload.getPaqueteId(), payload.getEstado());
        asignarRutaUseCase.asignarRuta(payload);
    }
}

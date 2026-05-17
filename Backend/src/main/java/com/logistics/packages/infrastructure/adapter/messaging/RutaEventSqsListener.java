package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.usecase.gestionnovedad.EventoRutaDto;
import com.logistics.packages.application.usecase.gestionnovedad.ProcesarEventoRutaUseCase;
import com.logistics.packages.domain.exception.EventoDuplicadoException;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RutaEventSqsListener {

    private final ProcesarEventoRutaUseCase procesarEventoRutaUseCase;

    @SqsListener("${aws.sqs.eventos-ruta-queue:eventos-ruta-queue}")
    public void onEventoRuta(EventoRutaDto eventoDto) {
        log.info("Evento de ruta recibido: {} para paquete: {}",
                eventoDto.getEventoId(), eventoDto.getPaqueteId());

        try {
            procesarEventoRutaUseCase.procesar(eventoDto);
            log.info("Evento de ruta procesado exitosamente: {}", eventoDto.getEventoId());
        } catch (EventoDuplicadoException e) {
            log.warn("Evento duplicado detectado y descartado: {}", e.getEventoId());
        } catch (Exception e) {
            log.error("Error al procesar evento de ruta {}: {}",
                    eventoDto.getEventoId(), e.getMessage(), e);
            throw new RuntimeException("Error al procesar evento de ruta", e);
        }
    }
}

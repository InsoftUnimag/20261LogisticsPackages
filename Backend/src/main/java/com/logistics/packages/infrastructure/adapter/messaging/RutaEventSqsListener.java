package com.logistics.packages.infrastructure.adapter.messaging;

import com.logistics.packages.application.usecase.gestionnovedad.EventoRutaDto;
import com.logistics.packages.application.usecase.gestionnovedad.ProcesarEventoRutaUseCase;
import com.logistics.packages.domain.exception.EventoDuplicadoException;
import com.logistics.packages.infrastructure.dto.event.EventoPaqueteM2Dto;
import com.logistics.packages.infrastructure.exception.SqsCommunicationException;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile({"default", "local", "aws"})
@RequiredArgsConstructor
public class RutaEventSqsListener {

    private final ProcesarEventoRutaUseCase procesarEventoRutaUseCase;
    private final EventoPaqueteM2Mapper eventoMapper;

    @SqsListener("${app.sqs.eventos-paquete-queue:logistics-eventos-paquete}")
    public void onEventoPaquete(EventoPaqueteM2Dto m2Dto) {
        log.info("Evento de paquete M2 recibido: {} para paquete: {}",
                m2Dto.getTipoEvento(), m2Dto.getPaqueteId());

        try {
            List<EventoRutaDto> comandos = eventoMapper.mapToEventoRuta(m2Dto);

            for (EventoRutaDto comando : comandos) {
                try {
                    procesarEventoRutaUseCase.procesar(comando);
                    log.info("Evento procesado exitosamente: {}", comando.getEventoId());
                } catch (EventoDuplicadoException e) {
                    log.warn("Evento duplicado detectado y descartado: {}", e.getEventoId());
                }
            }
        } catch (Exception e) {
            log.error("Error al procesar evento M2 {}: {}", m2Dto.getTipoEvento(), e.getMessage(), e);
            throw new SqsCommunicationException("Error al procesar evento de paquete M2", e);
        }
    }
}

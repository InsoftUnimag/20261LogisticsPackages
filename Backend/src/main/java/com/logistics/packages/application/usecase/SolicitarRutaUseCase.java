package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.application.ports.RutaQueuePort;
import com.logistics.packages.domain.event.SolicitudRutaEvent;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.Paquete;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitarRutaUseCase {

    private final PaqueteRepository paqueteRepository;
    private final RutaQueuePort rutaQueuePort;

    public void handle(SolicitudRutaEvent event) {
        log.info("Procesando solicitud de ruta para paquete: {}", event.getPaqueteId());

        Paquete paquete = paqueteRepository.findById(event.getPaqueteId())
                .orElseThrow(() -> new PaqueteNotFoundException(event.getPaqueteId()));

        log.info("Enviando solicitud de ruta para paquete {} con peso {}kg, volumen {}m3",
                paquete.getId(),
                paquete.getPeso() != null ? paquete.getPeso().getKilogramos() : null,
                paquete.getVolumenM3());

        rutaQueuePort.enviarSolicitud(paquete);

        log.info("Solicitud de ruta enviada exitosamente para paquete: {}", event.getPaqueteId());
    }
}

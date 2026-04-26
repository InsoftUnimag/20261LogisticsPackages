package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.application.ports.RutaQueuePort;
import com.logistics.packages.domain.event.SolicitudRutaEvent;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.infrastructure.dto.request.SolicitudRutaPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * T306 [P] [US3] - Caso de uso para solicitar ruta al Módulo de Gestión de Rutas
 * MOD1-IP-003 - Phase 2
 * 
 * Este caso de uso se invoca automáticamente después del pesaje exitoso de un paquete,
 * construyendo el payload JSON con la información del paquete y enviándolo de forma
 * asíncrona al Módulo de Gestión de Rutas.
 * 
 * FR-001: Comunicación estrictamente asíncrona
 * FR-002: Construir payload con datos completos del paquete
 * FR-004: Registrar cada intento
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitarRutaUseCase {

    private final PaqueteRepository paqueteRepository;
    private final RutaQueuePort rutaQueuePort;

    /**
     * Maneja el evento de solicitud de ruta para un paquete.
     * 
     * @param event El evento que contiene el ID del paquete
     * @throws PaqueteNotFoundException si el paquete no existe
     */
    public void handle(SolicitudRutaEvent event) {
        log.info("Procesando solicitud de ruta para paquete: {}", event.getPaqueteId());
        
        // Buscar el paquete existente
        Paquete paquete = paqueteRepository.findById(event.getPaqueteId())
                .orElseThrow(() -> new PaqueteNotFoundException(event.getPaqueteId()));

        // FR-002: Construir el payload JSON con los datos del paquete
        SolicitudRutaPayload payload = SolicitudRutaPayload.from(paquete);
        
        // FR-001: Enviar de forma asíncrona
        // FR-004: Registrar intento
        log.info("Enviando solicitud de ruta para paquete {} con peso {}kg, volumen {}m3",
                payload.getPaqueteId(),
                payload.getPesoKg(),
                payload.getVolumenM3());
        
        rutaQueuePort.enviarSolicitud(payload);
        
        log.info("Solicitud de ruta enviada exitosamente para paquete: {}", event.getPaqueteId());
    }
}

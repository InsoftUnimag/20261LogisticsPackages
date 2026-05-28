package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.HistorialEstadoRepository;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.HistorialEstado;
import com.logistics.packages.domain.model.Paquete;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AsignarRutaUseCase {

    private final PaqueteRepository paqueteRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    
    // ID del módulo de rutas para el registro de historial
    private static final UUID MODULO_RUTAS_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    public void asignarRuta(AsignarRutaCommand command) {
        log.info("Procesando asignación de ruta para paquete: {} / ruta: {}",
                command.paqueteId(), command.rutaId());

        Paquete paquete = paqueteRepository.findById(command.paqueteId())
                .orElseThrow(() -> new PaqueteNotFoundException(command.paqueteId()));

        paquete.asignarRuta(command.rutaId());

        paqueteRepository.save(paquete);
        
        // Registrar en el historial que la ruta fue asignada (evento informativo sin cambio de estado)
        HistorialEstado historial = new HistorialEstado(
                paquete.getId(),
                paquete.getEstado(), // estadoAnterior = estado actual (no hay cambio)
                paquete.getEstado(), // estadoNuevo = estado actual (no hay cambio)
                "Ruta asignada por Módulo de Gestión de Rutas: " + command.rutaId(),
                MODULO_RUTAS_ID,
                null // sin evidencia
        );
        historialEstadoRepository.guardar(historial);

        log.info("Ruta {} asignada al paquete {}. Estado: {}. Registrado en historial.",
                command.rutaId(), command.paqueteId(), paquete.getEstado());
    }
}

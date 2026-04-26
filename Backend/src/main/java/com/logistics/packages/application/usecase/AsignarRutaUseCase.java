package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.infrastructure.dto.response.RespuestaRutaPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * T307 [P] [US3] - Caso de uso para asignar ruta a un paquete
 * MOD1-IP-003 - Phase 2
 * 
 * Este caso de uso procesa la respuesta asíncrona del Módulo de Gestión de Rutas,
 * asignando el ID de ruta al paquete y actualizando su estado.
 * 
 * FR-003: Almacenar el ID de ruta en el paquete
 * FR-004: Registrar resultado de la asignación
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AsignarRutaUseCase {

    private final PaqueteRepository paqueteRepository;

    /**
     * Asigna una ruta a un paquete basándose en la respuesta del Módulo de Gestión de Rutas.
     * 
     * @param payload La respuesta del módulo de rutas con el rutaId
     * @throws PaqueteNotFoundException si el paquete no existe
     * @throws IllegalStateException si el paquete ya tiene una ruta asignada
     */
    public void asignarRuta(RespuestaRutaPayload payload) {
        log.info("Procesando respuesta de asignación de ruta para paquete: {} - Estado: {}",
                payload.getPaqueteId(), payload.getEstado());
        
        // Buscar el paquete existente
        Paquete paquete = paqueteRepository.findById(payload.getPaqueteId())
                .orElseThrow(() -> new PaqueteNotFoundException(payload.getPaqueteId()));

        // Verificar si la ruta fue asignada exitosamente
        if ("asignada".equalsIgnoreCase(payload.getEstado()) && payload.getRutaId() != null) {
            // FR-003: Almacenar el ID de ruta
            paquete.asignarRuta(payload.getRutaId());
            
            // Persistir los cambios
            paqueteRepository.save(paquete);
            
            log.info("Ruta {} asignada exitosamente al paquete {}. Nuevo estado: {}",
                    payload.getRutaId(),
                    payload.getPaqueteId(),
                    paquete.getEstado());
            
            if (payload.getTiempoEstimadoDias() != null) {
                log.info("Tiempo estimado de entrega: {} días", payload.getTiempoEstimadoDias());
            }
        } else {
            // FR-005: Manejar respuestas pendientes o con timeout
            log.warn("Asignación de ruta pendiente para paquete {}: {}",
                    payload.getPaqueteId(),
                    payload.getMensaje() != null ? payload.getMensaje() : "Sin mensaje");
            // No se asigna ruta ni se guarda. El sistema puede reintentar después.
        }
    }
}

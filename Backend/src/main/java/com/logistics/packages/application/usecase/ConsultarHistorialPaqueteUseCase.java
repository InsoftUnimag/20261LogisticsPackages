package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.HistorialEstadoRepository;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.HistorialEstado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para consultar el historial inmutable de estados de un paquete.
 * MOD1-UC-007: FR-001, SC-001 - Consulta síncrona del historial cronológico.
 * 
 * Este caso de uso orquesta:
 * - Validación de existencia del paquete
 * - Recuperación del historial inmutable
 * - Retorno ordenado cronológicamente
 * 
 * Principios aplicados: Single Responsibility, Dependency Inversion
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultarHistorialPaqueteUseCase {
    
    private final PaqueteRepository paqueteRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    
    /**
     * Consulta el historial completo de transiciones de estado para un paquete.
     * 
     * @param paqueteId ID del paquete cuyo historial se desea consultar
     * @return Lista de registros de historial ordenados cronológicamente por fecha de transición
     * @throws PaqueteNotFoundException si el paquete no existe en la base de datos
     */
    public List<HistorialEstado> consultarHistorial(UUID paqueteId) {
        log.info("Consultando historial de transiciones para paquete: {}", paqueteId);
        
        // Validar que el paquete existe (lanza PaqueteNotFoundException si no existe)
        paqueteRepository.findById(paqueteId)
                .orElseThrow(() -> new PaqueteNotFoundException(paqueteId));
        
        // Recuperar el historial inmutable (ya viene ordenado por fecha ascendente del adaptador)
        List<HistorialEstado> historial = historialEstadoRepository.obtenerHistorialPorPaqueteId(paqueteId);
        
        log.info("Historial consultado exitosamente: {} transiciones para paquete: {}", 
                historial.size(), paqueteId);
        
        return historial;
    }
}

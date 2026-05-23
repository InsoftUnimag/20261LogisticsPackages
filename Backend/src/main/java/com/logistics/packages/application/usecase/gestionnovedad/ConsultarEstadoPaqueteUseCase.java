package com.logistics.packages.application.usecase.gestionnovedad;

import com.logistics.packages.application.repository.HistorialEstadoRepository;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.model.HistorialEstado;
import com.logistics.packages.domain.model.Paquete;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para consultar el estado de un paquete.
 * MOD1-UC-007: FR-005, FR-006, FR-007 - Endpoint de consulta síncrona para Finanzas.
 * 
 * Este caso de uso proporciona información consolidada del paquete y su historial
 * para que el Módulo de Gestión de Finanzas pueda realizar ajustes financieros.
 * 
 * Principios aplicados: Single Responsibility
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultarEstadoPaqueteUseCase {
    
    private final PaqueteRepository paqueteRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    
    /**
     * Consulta el estado actual de un paquete con su historial completo.
     * 
     * @param rutaId ID de la ruta
     * @param paqueteId ID del paquete
     * @return GestionNovedadPaqueteResponse DTO con la información del paquete
     * @throws PaqueteNotFoundException si el paquete no existe
     */
    @Transactional(readOnly = true)
    public GestionNovedadPaqueteResponse consultar(UUID rutaId, UUID paqueteId) {
        log.info("Consultando estado del paquete: {} en ruta: {}", paqueteId, rutaId);
        
        // Buscar el paquete
        Paquete paquete = paqueteRepository.findById(paqueteId)
                .orElseThrow(() -> new PaqueteNotFoundException(paqueteId));
        
        // Validar que el paquete pertenezca a la ruta especificada
        if (paquete.getRutaId() != null && !paquete.getRutaId().equals(rutaId)) {
            log.warn("El paquete {} no pertenece a la ruta {}", paqueteId, rutaId);
            throw new IllegalArgumentException(
                    String.format("El paquete %s no pertenece a la ruta %s", paqueteId, rutaId)
            );
        }
        
        // Obtener el historial completo
        List<HistorialEstado> historial = historialEstadoRepository.obtenerHistorialPorPaqueteId(paqueteId);
        
        // Construir y retornar el response
        GestionNovedadPaqueteResponse response = GestionNovedadPaqueteResponse.builder()
                .idRoute(rutaId)
                .idPaquete(paqueteId)
                .estado(paquete.getEstado().name())
                .valorDeclarado(paquete.getValorDeclarado())
                .precioEnvio(paquete.getPrecioEnvio() != null ? paquete.getPrecioEnvio().getValor() : null)
                .metodoPago(paquete.getMetodoPago() != null ? paquete.getMetodoPago().name() : null)
                .fechaIngresoUtc(paquete.getFechaIngresoUtc())
                .fechaEntregaUtc(paquete.getFechaEntregaUtc())
                .urlEvidenciaEntrega(paquete.getUrlEvidenciaEntrega())
                .nombreFirmante(paquete.getNombreFirmante())
                .historialEstados(historial)
                .build();
        
        log.info("Consulta exitosa para paquete: {}", paqueteId);
        return response;
    }
}

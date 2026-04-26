package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.application.ports.ZonaDestinoRepository;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.exception.ZonaDestinoNotFoundException;
import com.logistics.packages.domain.exception.ZonaDestinoSaturadaException;
import com.logistics.packages.domain.exception.ZonaNoAptaException;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.model.ZonaDestino;
import com.logistics.packages.domain.service.CalculoZonaDestinoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso para clasificar paquetes por zona de destino.
 * MOD1-IP-005: T510
 * 
 * Responsabilidades:
 * - FR-001: Sugerir zona de destino basada en proximidad geográfica
 * - FR-002: Registrar clasificación lógica de zona de destino
 * - FR-003: Validar compatibilidad de mercancía con zona
 * - FR-004: Validar capacidad de la zona antes de confirmar
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClasificarPaqueteUseCase {

    private final PaqueteRepository paqueteRepository;
    private final ZonaDestinoRepository zonaDestinoRepository;
    private final CalculoZonaDestinoService calculoZonaService;

    /**
     * Sugiere una zona de destino para un paquete basándose en sus coordenadas.
     * 
     * @param paqueteId El ID del paquete a clasificar
     * @return Respuesta con la zona sugerida
     * @throws PaqueteNotFoundException si el paquete no existe
     */
    public ClasificacionSugeridaResponse sugerirZonaParaPaquete(UUID paqueteId) {
        log.info("Iniciando sugerencia de zona de destino para paquete {}", paqueteId);
        
        // 1. Obtener el paquete
        Paquete paquete = paqueteRepository.findById(paqueteId)
                .orElseThrow(() -> new PaqueteNotFoundException(paqueteId));
        
        // 2. Calcular la zona de destino usando el servicio de dominio
        ZonaDestino zonaSugerida = calculoZonaService.calcularZona(paquete);
        
        log.info("Zona sugerida: {} para paquete {}", zonaSugerida.getNombre(), paqueteId);
        
        // 3. Retornar la sugerencia
        return new ClasificacionSugeridaResponse(
                paqueteId, 
                zonaSugerida.getId(), 
                zonaSugerida.getNombre(),
                zonaSugerida.getCodigo()
        );
    }

    /**
     * Confirma la clasificación de un paquete en una zona de destino.
     * FR-002, FR-003, FR-004
     * 
     * @param paqueteId El ID del paquete
     * @param zonaDestinoId El ID de la zona de destino confirmada
     * @throws PaqueteNotFoundException si el paquete no existe
     * @throws ZonaDestinoNotFoundException si la zona no existe
     * @throws ZonaNoAptaException si la zona no es apta para el tipo de mercancía
     * @throws ZonaDestinoSaturadaException si la zona ha alcanzado su capacidad máxima
     */
    public void confirmarClasificacion(UUID paqueteId, UUID zonaDestinoId) {
        log.info("Confirmando clasificación. Paquete: {}, Zona: {}", paqueteId, zonaDestinoId);
        
        // 1. Obtener el paquete
        Paquete paquete = paqueteRepository.findById(paqueteId)
                .orElseThrow(() -> new PaqueteNotFoundException(paqueteId));
        
        // 2. Obtener la zona de destino
        ZonaDestino zona = zonaDestinoRepository.findById(zonaDestinoId)
                .orElseThrow(() -> new ZonaDestinoNotFoundException(zonaDestinoId));
        
        // 3. FR-003: Validar que la zona es apta para el tipo de mercancía
        if (paquete.getTipoMercancia() != null && !zona.esAptaPara(paquete.getTipoMercancia())) {
            log.error("La zona {} no es apta para mercancía de tipo {}", 
                    zona.getNombre(), paquete.getTipoMercancia());
            throw new ZonaNoAptaException(
                    zona.getId(), 
                    paquete.getTipoMercancia(), 
                    zona.getCategoria()
            );
        }
        
        // 4. FR-004: Validar que la zona tiene capacidad disponible
        if (!zona.tieneCapacidadDisponible()) {
            log.error("La zona {} ha alcanzado su capacidad máxima", zona.getNombre());
            throw new ZonaDestinoSaturadaException(
                    zona.getId(), 
                    zona.getNombre(), 
                    null // TODO: Implementar zona de desborde
            );
        }
        
        // 5. FR-002: Asignar zona de destino al paquete (cambia estado a LISTO_PARA_DESPACHO)
        paquete.asignarZonaDestino(zona.getId());
        
        // 6. Incrementar contador de la zona
        zona.incrementarContador();
        
        // 7. Guardar cambios
        paqueteRepository.save(paquete);
        zonaDestinoRepository.save(zona);
        
        log.info("Clasificación confirmada. Paquete {} asignado a zona {}. Estado: {}", 
                paquete.getId(), zona.getNombre(), paquete.getEstado());
    }
}

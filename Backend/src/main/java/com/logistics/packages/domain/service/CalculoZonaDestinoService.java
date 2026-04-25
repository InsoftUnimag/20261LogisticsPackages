package com.logistics.packages.domain.service;

import com.logistics.packages.application.ports.ZonaDestinoRepository;
import com.logistics.packages.domain.exception.ZonaDestinoNoEncontradaException;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.model.ZonaDestino;
import com.logistics.packages.domain.valueobject.Coordenadas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de dominio para calcular la zona de destino de un paquete.
 * MOD1-IP-005: T507
 * 
 * Responsabilidades:
 * - FR-001: Determinar la zona de destino basándose en la proximidad geográfica
 * - Lógica pura de negocio sin dependencias de infraestructura
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalculoZonaDestinoService {

    private final ZonaDestinoRepository zonaDestinoRepository;

    /**
     * Calcula la zona de destino apropiada para un paquete basándose en sus coordenadas.
     * 
     * @param paquete El paquete para el cual calcular la zona
     * @return La zona de destino calculada
     * @throws IllegalArgumentException si el paquete no tiene coordenadas
     * @throws ZonaDestinoNoEncontradaException si no se encuentra una zona para las coordenadas
     */
    public ZonaDestino calcularZona(Paquete paquete) {
        log.debug("Calculando zona de destino para paquete {}", paquete.getId());
        
        // Validar que el paquete tenga coordenadas
        Coordenadas coordenadas = paquete.getCoordenadas();
        if (coordenadas == null) {
            throw new IllegalArgumentException(
                "El paquete debe tener coordenadas asignadas para calcular la zona de destino."
            );
        }

        // Obtener todas las zonas activas
        List<ZonaDestino> zonasActivas = zonaDestinoRepository.findAllActivas();
        
        if (zonasActivas.isEmpty()) {
            log.error("No hay zonas de destino configuradas en el sistema");
            throw new ZonaDestinoNoEncontradaException(
                "No hay zonas de destino configuradas en el sistema."
            );
        }

        // Buscar la primera zona que contenga las coordenadas
        ZonaDestino zonaEncontrada = zonasActivas.stream()
                .filter(zona -> zona.contieneCoordenas(coordenadas))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("No se encontró zona de destino para coordenadas: lat={}, lon={}",
                            coordenadas.latitud(), coordenadas.longitud());
                    return new ZonaDestinoNoEncontradaException(coordenadas);
                });

        log.info("Zona de destino calculada: {} para paquete {}", 
                zonaEncontrada.getNombre(), paquete.getId());
        
        return zonaEncontrada;
    }
}

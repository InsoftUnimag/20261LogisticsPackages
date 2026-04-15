package com.logistics.packages.application.usecase;

import com.logistics.packages.application.ports.out.GeocodingService;
import com.logistics.packages.application.ports.out.PaqueteRepository;
import com.logistics.packages.application.ports.out.RutaEventPublisher;
import com.logistics.packages.domain.model.EstadoGps;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.Coordenadas;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrarAdmisionUseCase {

    private final PaqueteRepository paqueteRepository;
    private final GeocodingService geocodingService;
    private final RutaEventPublisher eventPublisher;

    @Transactional
    public UUID registrarAdmision(Paquete paquete) {
        // FR-002: Intentar Geocoding con timeout (el timeout debe manejarse en el adaptador)
        Optional<Coordenadas> coordenadas = geocodingService.localizar(paquete.getDireccionDestino());
        
        coordenadas.ifPresent(paquete::asignarCoordenadas);

        // FR-011: Nota - Se asume que el pesaje ya se validó antes de llamar a este UC o es parte del flujo.
        // El IP indica que el evento de ruta se dispara si hay coordenadas y se ejecutó el pesaje.
        
        Paquete guardado = paqueteRepository.guardar(paquete);

        // FR-012: Invocar Solicitar Ruta si el GPS está resuelto
        if (guardado.getEstadoGps() == EstadoGps.RESUELTO) {
            eventPublisher.publicarSolicitudRuta(guardado.getId());
        }

        return guardado.getId();
    }
}

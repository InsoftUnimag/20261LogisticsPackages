package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.RegistrarAdmisionIn;
import com.logistics.packages.application.repository.GeocodingService;
import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.application.repository.RutaEventPublisher;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.Coordenadas;
import com.logistics.packages.domain.valueobject.MetodoPago;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class RegistrarAdmisionUseCase implements RegistrarAdmisionIn {

    private final PaqueteRepository paqueteRepository;
    private final GeocodingService geocodingService;
    private final RutaEventPublisher eventPublisher;

    @Override
    public UUID registrarAdmision(RegistroAdmisionCommand command) {
        Coordenadas coordenadas = null;
        Optional<Coordenadas> coordenadasOpt = geocodingService.localizar(command.direccionDestino());
        if (coordenadasOpt.isEmpty()) {
            // El fallback: El empleado deberá llenar las coordenadas a mano
            // a través de React posteriormente al guardado parcial.
            // Se podría lanzar una excepción o manejarlo como estado PENDIENTE_GPS
        }
        coordenadas = coordenadasOpt.orElse(null);

        Paquete paquete = Paquete.builder()
                .sedeId(command.sedeId())
                .direccionDestino(command.direccionDestino())
                .valorDeclarado(command.valorDeclarado())
                .metodoPago(MetodoPago.PREPAGO)
                .remitente(command.remitente())
                .destinatario(command.destinatario())
                .tipoMercancia(command.tipoMercancia())
                .indicadorFormaIrregular(command.indicadorFormaIrregular())
                .build();
        paquete.prePersist();

        if (coordenadas != null) {
            paquete.asignarCoordenadas(coordenadas);
        }

        // Procesar pesaje si los datos están presentes
        if (haEjecutadoPesaje(command)) {
            // El factor de conversión debería ser configurable
            paquete.procesarPesaje(command.peso(), command.largo(), command.ancho(), command.alto(), 250);
        }

        Paquete saved = paqueteRepository.save(paquete);

        if (coordenadas != null && haEjecutadoPesaje(command)) {
            eventPublisher.publicarSolicitudRuta(saved.getId());
        }

        return saved.getId();
    }

    private boolean haEjecutadoPesaje(RegistroAdmisionCommand command) {
        return command.peso() != null && command.largo() != null && command.ancho() != null && command.alto() != null;
    }
}

package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.*;
import com.logistics.packages.domain.exception.InvalidCoverageException;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class RegistrarAdmisionUseCase implements RegistrarAdmisionIn {

    private final PaqueteRepository paqueteRepository;
    private final GeocodingService geocodingService;
    private final RutaEventPublisher eventPublisher;
    private final CoverageService coverageService;
    private final PriceCalculationService priceCalculationService;
    private final DistanceService distanceService;

    @Override
    public UUID registrarAdmision(RegistroAdmisionCommand command) {
        Coordenadas coordenadas = null;

        if (command.coordenadasManuales() != null) {
            coordenadas = command.coordenadasManuales();
        } else {
            Optional<Coordenadas> coordenadasOpt = geocodingService.localizar(command.direccionDestino());
            coordenadas = coordenadasOpt.orElse(null);
        }

        if (coordenadas != null && coverageService.isWithinCoverage(coordenadas)) {
            Paquete paquete = Paquete.builder()
                    .sedeId(command.sedeId())
                    .direccionDestino(command.direccionDestino())
                    .valorDeclarado(command.valorDeclarado())
                    .metodoPago(command.metodoPago())
                    .remitente(command.remitente())
                    .destinatario(command.destinatario())
                    .tipoMercancia(command.tipoMercancia())
                    .indicadorFormaIrregular(command.indicadorFormaIrregular())
                    .build();
            paquete.prePersist();
            paquete.asignarCoordenadas(coordenadas);

            if (haEjecutadoPesaje(command)) {
                Peso peso = new Peso(command.peso());
                Dimensiones dimensiones = new Dimensiones(command.largo(), command.ancho(), command.alto());
                paquete.procesarPesaje(peso, dimensiones, command.tipoMercancia(), command.indicadorFormaIrregular());

                double distanciaKm = distanceService.calcularDistanciaDesdeSede(coordenadas);
                paquete.setDistanciaEstimadaKm(distanciaKm);

                BigDecimal precio = priceCalculationService.calculatePrice(paquete);
                paquete.asignarPrecio(precio);
            }

            Paquete saved = paqueteRepository.save(paquete);

            if (haEjecutadoPesaje(command)) {
                eventPublisher.publicarSolicitudRuta(saved.getId());
            }

            return saved.getId();
        }

        throw new InvalidCoverageException(command.direccionDestino());
    }

    private boolean haEjecutadoPesaje(RegistroAdmisionCommand command) {
        return command.peso() != null && command.largo() != null && command.ancho() != null && command.alto() != null;
    }
}

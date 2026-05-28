package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.*;
import com.logistics.packages.domain.event.SolicitudRutaEvent;
import com.logistics.packages.domain.exception.InvalidCoverageException;
import com.logistics.packages.domain.model.HistorialEstado;
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
    private final SolicitarRutaUseCase solicitarRutaUseCase;
    private final CoverageService coverageService;
    private final PriceCalculationService priceCalculationService;
    private final DistanceService distanceService;
    private final HistorialEstadoRepository historialEstadoRepository;
    
    // ID del sistema para registros de transiciones automáticas
    private static final UUID SISTEMA_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

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
            UUID paqueteId = UUID.randomUUID();
            Paquete paquete = Paquete.crearNuevo(
                    paqueteId,
                    command.sedeId(),
                    command.direccionDestino(),
                    command.valorDeclarado(),
                    command.metodoPago(),
                    command.remitente(),
                    command.destinatario(),
                    command.tipoMercancia(),
                    command.indicadorFormaIrregular()
            );
            paquete.asignarCoordenadas(coordenadas);

            if (haEjecutadoPesaje(command)) {
                Peso peso = new Peso(command.peso());
                Dimensiones dimensiones = new Dimensiones(command.largo(), command.ancho(), command.alto());
                paquete.procesarPesaje(peso, dimensiones, command.tipoMercancia(), command.indicadorFormaIrregular());

                double distanciaKm = distanceService.calcularDistanciaDesdeSede(coordenadas);
                BigDecimal precio = priceCalculationService.calculatePrice(paquete, distanciaKm);
                paquete.asignarPrecioEnvio(precio, distanciaKm);
            }

            Paquete saved = paqueteRepository.save(paquete);
            
            // Registrar en el historial la transición inicial: RECIBIDO_EN_SEDE
            HistorialEstado historialInicial = new HistorialEstado(
                    saved.getId(),
                    null, // No hay estado anterior (es el inicio)
                    saved.getEstado(), // RECIBIDO_EN_SEDE
                    "Paquete recibido en sede",
                    SISTEMA_ID,
                    null // Sin evidencia para admisión
            );
            historialEstadoRepository.guardar(historialInicial);

            if (haEjecutadoPesaje(command)) {
                SolicitudRutaEvent evento = SolicitudRutaEvent.of(saved.getId());
                solicitarRutaUseCase.handle(evento);
            }

            return saved.getId();
        }

        throw new InvalidCoverageException(command.direccionDestino());
    }

    private boolean haEjecutadoPesaje(RegistroAdmisionCommand command) {
        return command.peso() != null && command.largo() != null && command.ancho() != null && command.alto() != null;
    }
}

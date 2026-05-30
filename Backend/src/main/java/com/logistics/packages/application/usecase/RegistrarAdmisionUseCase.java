package com.logistics.packages.application.usecase;

import com.logistics.packages.application.ports.SedeRepository;
import com.logistics.packages.application.repository.*;
import com.logistics.packages.domain.event.SolicitudRutaEvent;
import com.logistics.packages.domain.exception.InvalidCoverageException;
import com.logistics.packages.domain.exception.TimeoutGeocodingException;
import com.logistics.packages.domain.model.HistorialEstado;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.model.Sede;
import com.logistics.packages.domain.valueobject.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class RegistrarAdmisionUseCase implements RegistrarAdmisionIn {

    private static final Logger log = LoggerFactory.getLogger(RegistrarAdmisionUseCase.class);

    private final PaqueteRepository paqueteRepository;
    private final GeocodingService geocodingService;
    private final SolicitarRutaUseCase solicitarRutaUseCase;
    private final CoverageService coverageService;
    private final PriceCalculationService priceCalculationService;
    private final DistanceService distanceService;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final SedeRepository sedeRepository;
    private final ArchivoStoragePort archivoStoragePort;
    
    @Override
    public UUID registrarAdmision(RegistroAdmisionCommand command) {
        Coordenadas coordenadas = null;
        boolean geolocalizacionFallo = false;

        if (command.coordenadasManuales() != null) {
            coordenadas = command.coordenadasManuales();
        } else {
            try {
                Optional<Coordenadas> coordenadasOpt = geocodingService.localizar(command.direccionDestino());
                coordenadas = coordenadasOpt.orElse(null);
            } catch (TimeoutGeocodingException e) {
                // FE-5 + Contingencia GPS: Si hay timeout, registrar paquete con GPS PENDIENTE
                log.warn("Timeout en geocoding para dirección: {}. Paquete será registrado con GPS PENDIENTE", 
                    command.direccionDestino().getDireccionCompleta(), e);
                geolocalizacionFallo = true;
            }
        }

        // Si hay coordenadas válidas Y están dentro de cobertura, proceder normalmente
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

            // Bug Fix: Obtener coordenadas reales de la sede seleccionada en lugar de usar hardcodeadas
            Optional<Sede> sedeOpt = sedeRepository.findById(command.sedeId());
            Coordenadas coordenadasSede = sedeOpt
                    .map(sede -> new Coordenadas(sede.getLatitud(), sede.getLongitud()))
                    .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada: " + command.sedeId()));
            
            // Bug Fix: Calcular distancia SIEMPRE que hay coordenadas, no solo con pesaje
            double distanciaKm = distanceService.calcularDistanciaDesdeSede(coordenadasSede, coordenadas);

            if (haEjecutadoPesaje(command)) {
                Peso peso = new Peso(command.peso());
                Dimensiones dimensiones = new Dimensiones(command.largo(), command.ancho(), command.alto());
                paquete.procesarPesaje(peso, dimensiones, command.tipoMercancia(), command.indicadorFormaIrregular());

                BigDecimal precio = priceCalculationService.calculatePrice(paquete, distanciaKm);
                paquete.asignarPrecioEnvio(precio, distanciaKm);
            } else {
                // Bug Fix: Asignar la distancia incluso sin pesaje, con precio provisional 0
                paquete.asignarPrecioEnvio(BigDecimal.ZERO, distanciaKm);
            }

            Paquete saved = paqueteRepository.save(paquete);
            
            // Subir evidencia fotográfica si se proporcionó
            String urlEvidencia = null;
            if (command.evidencia() != null && !command.evidencia().isEmpty()) {
                urlEvidencia = archivoStoragePort.guardar(
                    "admision",
                    saved.getId().toString(),
                    command.evidencia()
                );
            }

            // Registrar en el historial la transición inicial: RECIBIDO_EN_SEDE
            // Bug Fix: Usar usuarioId del comando en lugar del hardcodeado SISTEMA_ID
            HistorialEstado historialInicial = new HistorialEstado(
                    saved.getId(),
                    null, // No hay estado anterior (es el inicio)
                    saved.getEstado(), // RECIBIDO_EN_SEDE
                    "Paquete recibido en sede",
                    command.usuarioId(),
                    urlEvidencia
            );
            historialEstadoRepository.guardar(historialInicial);

            if (haEjecutadoPesaje(command)) {
                SolicitudRutaEvent evento = SolicitudRutaEvent.of(saved.getId());
                solicitarRutaUseCase.handle(evento);
            }

            return saved.getId();
        }

        // Si falló la geolocalización (timeout) o no hay coordenadas, crear paquete con GPS PENDIENTE
        if (geolocalizacionFallo || coordenadas == null) {
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
            // NO asignar coordenadas → quedará con estadoGps = PENDIENTE

            Paquete saved = paqueteRepository.save(paquete);
            
            // Subir evidencia fotográfica si se proporcionó
            String urlEvidencia = null;
            if (command.evidencia() != null && !command.evidencia().isEmpty()) {
                urlEvidencia = archivoStoragePort.guardar(
                    "admision",
                    saved.getId().toString(),
                    command.evidencia()
                );
            }

            // Registrar en el historial: Paquete recibido con GPS PENDIENTE
            String motivo = geolocalizacionFallo 
                ? "Paquete recibido con geolocalización pendiente (timeout en servicio)"
                : "Paquete recibido con geolocalización no disponible";
            
            HistorialEstado historialInicial = new HistorialEstado(
                    saved.getId(),
                    null,
                    saved.getEstado(), // RECIBIDO_EN_SEDE
                    motivo,
                    command.usuarioId(),
                    urlEvidencia
            );
            historialEstadoRepository.guardar(historialInicial);

            log.info("Paquete {} registrado con GPS PENDIENTE. Operador debe ingresar coordenadas manualmente.", saved.getId());
            return saved.getId();
        }

        throw new InvalidCoverageException(command.direccionDestino());
    }

    private boolean haEjecutadoPesaje(RegistroAdmisionCommand command) {
        return command.peso() != null && command.largo() != null && command.ancho() != null && command.alto() != null;
    }
}
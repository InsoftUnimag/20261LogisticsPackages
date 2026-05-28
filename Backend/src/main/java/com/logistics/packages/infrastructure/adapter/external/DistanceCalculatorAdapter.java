package com.logistics.packages.infrastructure.adapter.external;

import com.logistics.packages.application.repository.DistanceService;
import com.logistics.packages.domain.valueobject.Coordenadas;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DistanceCalculatorAdapter implements DistanceService {

    private static final double RADIO_TIERRA_KM = 6371.0;

    @Value("${app.sede.latitud:4.624335}")
    private double sedeLatitud;

    @Value("${app.sede.longitud:-74.063644}")
    private double sedeLongitud;

    @Override
    public double calcularDistanciaKm(Coordenadas origen, Coordenadas destino) {
        double distancia = haversine(origen.latitud(), origen.longitud(), destino.latitud(), destino.longitud());
        log.info("Distancia calculada: {} km", distancia);
        return distancia;
    }

    @Override
    public double calcularDistanciaDesdeSede(Coordenadas destino) {
        double distancia = haversine(sedeLatitud, sedeLongitud, destino.latitud(), destino.longitud());
        log.info("Distancia desde sede ({}): {} km", distancia, sedeLatitud);
        return distancia;
    }

    @Override
    public double calcularDistanciaDesdeSede(Coordenadas origen, Coordenadas destino) {
        double distancia = haversine(origen.latitud(), origen.longitud(), destino.latitud(), destino.longitud());
        log.info("Distancia desde ({}, {}) a ({}, {}): {} km", 
            origen.latitud(), origen.longitud(), 
            destino.latitud(), destino.longitud(), 
            distancia);
        return distancia;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_KM * c;
    }
}
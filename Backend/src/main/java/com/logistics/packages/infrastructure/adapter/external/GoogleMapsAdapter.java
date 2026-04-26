package com.logistics.packages.infrastructure.adapter.external;

import com.logistics.packages.application.repository.GeocodingService;
import com.logistics.packages.domain.exception.TimeoutGeocodingException;
import com.logistics.packages.domain.valueobject.Coordenadas;
import com.logistics.packages.domain.valueobject.Direccion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class GoogleMapsAdapter implements GeocodingService {

    @Value("${app.geocoding.timeout-ms:5000}")
    private long timeoutMs;

    @Override
    public Optional<Coordenadas> localizar(Direccion direccion) throws TimeoutGeocodingException {
        log.info("Geocoding address: {} with timeout: {}ms", direccion.getDireccion(), timeoutMs);

        try {
            Optional<Coordenadas> resultado = Optional.of(new Coordenadas(11.22, -74.18));

            Thread.sleep(timeoutMs);

            return resultado;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
throw new TimeoutGeocodingException("Geocoding exceeded timeout of " + timeoutMs + "ms");
        }
    }
}

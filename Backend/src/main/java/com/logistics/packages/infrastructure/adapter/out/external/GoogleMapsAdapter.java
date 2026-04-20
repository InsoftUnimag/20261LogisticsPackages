package com.logistics.packages.infrastructure.adapter.out.external;

import com.logistics.packages.application.ports.out.GeocodingService;
import com.logistics.packages.domain.exception.TimeoutGeocodingException;
import com.logistics.packages.domain.valueobject.Coordenadas;
import com.logistics.packages.domain.valueobject.Direccion;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GoogleMapsAdapter implements GeocodingService {

    @Override
    public Optional<Coordenadas> localizar(Direccion direccion) throws TimeoutGeocodingException {
        // Placeholder implementation
        // In a real implementation, this would call the Google Maps API
        // and handle timeouts.
        System.out.println("Geocoding address: " + direccion.getDireccion());
        // Simulate a successful geocoding
        return Optional.of(new Coordenadas(11.22, -74.18));
    }
}

package com.logistics.packages.infrastructure.adapter.external;

import com.logistics.packages.application.repository.GeocodingService;
import com.logistics.packages.domain.exception.CoordenadasInvalidasException;
import com.logistics.packages.domain.exception.TimeoutGeocodingException;
import com.logistics.packages.domain.valueobject.Coordenadas;
import com.logistics.packages.domain.valueobject.Direccion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class GoogleMapsAdapter implements GeocodingService {

    private final RestTemplate restTemplate;
    private final String apiKey;

    public GoogleMapsAdapter(
            RestTemplate restTemplate,
            @Value("${app.geocoding.api-key:}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    @Override
    public Optional<Coordenadas> localizar(Direccion direccion) throws TimeoutGeocodingException {
        String direccionTexto = direccion.getDireccionCompleta();
        log.info("Geocoding address: {}", direccionTexto);

        try {
            String url = buildGeocodingUrl(direccionTexto);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseCoordenadas(response.getBody());
            }

            log.warn("Respuesta vacía o estado no exitoso del API de Google Maps");
            return Optional.empty();

        } catch (RestClientException e) {
            if (e.getCause() != null && 
                e.getCause().getMessage() != null && 
                e.getCause().getMessage().contains("Timeout")) {
                throw new TimeoutGeocodingException("Tiempo de espera excedido al consultar Google Maps API");
            }
            log.error("Error al consultar Google Maps API: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String buildGeocodingUrl(String direccion) {
        String direccionCodificada = URLEncoder.encode(direccion, StandardCharsets.UTF_8);
        return "https://maps.googleapis.com/maps/api/geocode/json?address=" 
               + direccionCodificada 
               + "&key=" 
               + apiKey;
    }

    @SuppressWarnings("unchecked")
    private Optional<Coordenadas> parseCoordenadas(Map<String, Object> response) {
        String status = (String) response.get("status");

        if (!"OK".equals(status)) {
            log.warn("Estado de geocoding no exitoso: {}", status);
            if ("ZERO_RESULTS".equals(status)) {
                log.info("No se encontraron resultados para la dirección");
            }
            return Optional.empty();
        }

        var results = (java.util.List<Map<String, Object>>) response.get("results");
        if (results == null || results.isEmpty()) {
            log.warn("No hay resultados en la respuesta");
            return Optional.empty();
        }

        Map<String, Object> firstResult = results.get(0);
        Map<String, Object> geometry = (Map<String, Object>) firstResult.get("geometry");
        if (geometry == null) {
            log.warn("Geometry no encontrado en la respuesta");
            return Optional.empty();
        }

        Map<String, Object> location = (Map<String, Object>) geometry.get("location");
        if (location == null) {
            log.warn("Location no encontrado en geometry");
            return Optional.empty();
        }

        Double lat = (Double) location.get("lat");
        Double lng = (Double) location.get("lng");

        if (lat == null || lng == null) {
            log.warn("Coordenadas no válidas en la respuesta");
            return Optional.empty();
        }

        log.info("Coordenadas obtenidas: lat={}, lng={}", lat, lng);

        try {
            return Optional.of(new Coordenadas(lat, lng));
        } catch (CoordenadasInvalidasException e) {
            log.error("Coordenadas fuera de rango válido: {}", e.getMessage());
            return Optional.empty();
        }
    }
}

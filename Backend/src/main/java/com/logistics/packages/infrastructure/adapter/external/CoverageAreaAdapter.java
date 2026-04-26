package com.logistics.packages.infrastructure.adapter.external;

import com.logistics.packages.application.repository.CoverageService;
import com.logistics.packages.domain.valueobject.Coordenadas;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CoverageAreaAdapter implements CoverageService {

    @Value("${app.coverage.latitud-min:4.0}")
    private double latitudMin;

    @Value("${app.coverage.latitud-max:5.0}")
    private double latitudMax;

    @Value("${app.coverage.longitud-min:-75.0}")
    private double longitudMin;

    @Value("${app.coverage.longitud-max:-74.0}")
    private double longitudMax;

    @Override
    public boolean isWithinCoverage(Coordenadas coordenadas) {
        if (coordenadas == null) {
            log.warn("Coordenadas nulas, fuera de cobertura");
            return false;
        }

        boolean dentro = coordenadas.latitud() >= latitudMin
                && coordenadas.latitud() <= latitudMax
                && coordenadas.longitud() >= longitudMin
                && coordenadas.longitud() <= longitudMax;

        if (!dentro) {
            log.info("Coordenadas {} fuera del área de cobertura [{},{}]x[{},{}]",
                    coordenadas, latitudMin, latitudMax, longitudMin, longitudMax);
        }

        return dentro;
    }
}
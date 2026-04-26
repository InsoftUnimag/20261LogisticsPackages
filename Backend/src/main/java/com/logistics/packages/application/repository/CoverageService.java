package com.logistics.packages.application.repository;

import com.logistics.packages.domain.valueobject.Coordenadas;
import org.springframework.stereotype.Repository;

@Repository
public interface CoverageService {
    boolean isWithinCoverage(Coordenadas coordenadas);
}

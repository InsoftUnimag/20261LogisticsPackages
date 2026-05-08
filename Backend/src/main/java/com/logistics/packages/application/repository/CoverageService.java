package com.logistics.packages.application.repository;

import com.logistics.packages.domain.valueobject.Coordenadas;

public interface CoverageService {
    boolean isWithinCoverage(Coordenadas coordenadas);
}
package com.logistics.packages.application.repository;

import com.logistics.packages.domain.valueobject.Coordenadas;

public interface DistanceService {
    double calcularDistanciaKm(Coordenadas origen, Coordenadas destino);
    double calcularDistanciaDesdeSede(Coordenadas destino);
    // Nuevo método: calcula distancia desde coordenadas de sede explícitas
    double calcularDistanciaDesdeSede(Coordenadas origen, Coordenadas destino);
}

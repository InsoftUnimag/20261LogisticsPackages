package com.logistics.packages.application.ports.out;

import com.logistics.packages.domain.valueobject.Coordenadas;
import java.util.Optional;

public interface GeocodingService {
    Optional<Coordenadas> localizar(String direccion);
}

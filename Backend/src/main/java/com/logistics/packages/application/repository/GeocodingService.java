package com.logistics.packages.application.admision.repositories;

import com.logistics.packages.domain.valueobject.Coordenadas;
import com.logistics.packages.domain.valueobject.Direccion;
import java.util.Optional;

public interface GeocodingService {
    Optional<Coordenadas> localizar(Direccion direccion);
}

package com.logistics.packages.domain.exception;

import com.logistics.packages.domain.valueobject.Direccion;

public class InvalidCoverageException extends RuntimeException {

    public InvalidCoverageException(Direccion direccion) {
        super("La dirección '" + direccion.getDireccionCompleta() + "' está fuera de la zona de cobertura.");
    }
}

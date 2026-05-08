package com.logistics.packages.domain.exception;

public class CoordenadasInvalidasException extends RuntimeException {

    public CoordenadasInvalidasException(String mensaje) {
        super(mensaje);
    }
}
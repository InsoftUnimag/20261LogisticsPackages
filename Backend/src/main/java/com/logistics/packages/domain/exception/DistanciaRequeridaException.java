package com.logistics.packages.domain.exception;

public class DistanciaRequeridaException extends RuntimeException {

    public DistanciaRequeridaException() {
        super("El paquete debe tener distancia estimada para calcular el precio.");
    }
}
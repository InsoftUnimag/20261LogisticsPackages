package com.logistics.packages.domain.exception;

import com.logistics.packages.domain.valueobject.Coordenadas;

/**
 * Excepción lanzada cuando no se puede determinar una zona de destino para unas coordenadas.
 * MOD1-IP-005
 */
public class ZonaDestinoNoEncontradaException extends RuntimeException {
    
    public ZonaDestinoNoEncontradaException(Coordenadas coordenadas) {
        super(String.format(
            "No se encontró una zona de destino para las coordenadas: lat=%f, lon=%f",
            coordenadas.latitud(), coordenadas.longitud()
        ));
    }

    public ZonaDestinoNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}

package com.logistics.packages.domain.valueobject;

import lombok.Getter;

import java.util.UUID;

@Getter
public class NovedadBodega {
    private final TipoNovedad tipo;
    private final String observaciones;
    private final UUID usuarioId;
    private final String urlEvidencia;

    public NovedadBodega(TipoNovedad tipo, String observaciones, UUID usuarioId, String urlEvidencia) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de novedad es requerido");
        }
        if (observaciones == null || observaciones.isBlank()) {
            throw new IllegalArgumentException("Las observaciones son requeridas");
        }
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario es requerido");
        }
        this.tipo = tipo;
        this.observaciones = observaciones;
        this.usuarioId = usuarioId;
        this.urlEvidencia = urlEvidencia;
    }
}

package com.logistics.packages.domain.valueobject;

public enum CategoriaZona {
    NORMAL,
    DELICADA,
    ALTO_RIESGO,
    RETENCION;

    public boolean esCompatible(TipoMercancia tipoMercancia) {
        if (tipoMercancia == null) {
            return false;
        }
        return switch (tipoMercancia) {
            case PELIGROSO -> this == ALTO_RIESGO;
            case FRAGIL -> this == DELICADA || this == ALTO_RIESGO;
            case ESTANDAR -> this == NORMAL || this == DELICADA || this == ALTO_RIESGO;
            default -> false;
        };
    }
}

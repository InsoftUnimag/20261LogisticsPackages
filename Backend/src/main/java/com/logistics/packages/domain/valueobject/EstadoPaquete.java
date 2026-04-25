package com.logistics.packages.domain.valueobject;

public enum EstadoPaquete {
    RECIBIDO_EN_SEDE,
    EN_CLASIFICACION,
    NOVEDAD_EN_BODEGA,  // MOD1-UC-006: Estado para paquetes con novedades (dañados o extraviados)
    LISTO_PARA_DESPACHO,
    EN_TRANSITO,
    ENTREGADO;
    
    /**
     * FR-006: Verifica si el estado permite registrar una novedad en bodega.
     * Solo se permiten novedades en estados previos al tránsito.
     * 
     * @return true si el estado permite registrar una novedad
     */
    public boolean permiteNovedadEnBodega() {
        return this == RECIBIDO_EN_SEDE || this == EN_CLASIFICACION;
    }
}

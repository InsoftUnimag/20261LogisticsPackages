package com.logistics.packages.domain.valueobject;

public enum EstadoPaquete {
    RECIBIDO_EN_SEDE,
    EN_CLASIFICACION,
    NOVEDAD_EN_BODEGA,  // MOD1-UC-006: Estado para paquetes con novedades (dañados o extraviados)
    LISTO_PARA_DESPACHO,
    EN_TRANSITO,  // MOD1-UC-007: Paquete en ruta
    EN_PARADA_DE_ENTREGA,  // MOD1-UC-007: Transportador en el sitio de entrega
    ENTREGADO,  // MOD1-UC-007: Paquete entregado exitosamente
    DEVOLUCION_EN_RUTA,  // MOD1-UC-007: Paquete en devolución desde ruta
    EXTRAVIADO_EN_RUTA,  // MOD1-UC-007: Paquete extraviado en campo
    DAÑADO_EN_RUTA;  // MOD1-UC-007: Paquete dañado en campo
    
    /**
     * FR-006: Verifica si el estado permite registrar una novedad en bodega.
     * Solo se permiten novedades en estados previos al tránsito.
     * 
     * @return true si el estado permite registrar una novedad
     */
    public boolean permiteNovedadEnBodega() {
        return this == RECIBIDO_EN_SEDE || this == EN_CLASIFICACION;
    }
    
    /**
     * MOD1-UC-007: Verifica si el estado es una transición válida desde el estado actual.
     * Previene transiciones ilógicas (ej. de ENTREGADO a EN_TRANSITO).
     * 
     * @param estadoActual El estado actual del paquete
     * @return true si la transición es válida
     */
    public boolean esTransicionValidaDesde(EstadoPaquete estadoActual) {
        if (estadoActual == null) {
            return false;
        }
        
        // No se puede retroceder desde ENTREGADO
        if (estadoActual == ENTREGADO) {
            return false;
        }
        
        // Validaciones específicas según el estado destino
        switch (this) {
            case EN_TRANSITO:
                return estadoActual == LISTO_PARA_DESPACHO;
            case EN_PARADA_DE_ENTREGA:
                return estadoActual == EN_TRANSITO;
            case ENTREGADO:
                return estadoActual == EN_PARADA_DE_ENTREGA;
            case DEVOLUCION_EN_RUTA:
                return estadoActual == EN_TRANSITO || estadoActual == EN_PARADA_DE_ENTREGA;
            case EXTRAVIADO_EN_RUTA:
            case DAÑADO_EN_RUTA:
                return estadoActual == EN_TRANSITO || estadoActual == EN_PARADA_DE_ENTREGA;
            default:
                return true;
        }
    }
}

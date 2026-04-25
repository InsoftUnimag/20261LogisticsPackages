package com.logistics.packages.domain.valueobject;

/**
 * Estados del ciclo de vida de un paquete.
 * Los estados siguen un flujo secuencial desde la admisión hasta la entrega.
 */
public enum EstadoPaquete {
    /**
     * Paquete recibido en la sede inicial
     */
    RECIBIDO_EN_SEDE,
    
    /**
     * Paquete en proceso de clasificación en bodega
     */
    EN_CLASIFICACION,
    
    /**
     * Paquete listo para ser despachado
     */
    LISTO_PARA_DESPACHO,
    
    /**
     * Paquete reportado con novedad en bodega (dañado o extraviado)
     * MOD1-IP-006
     */
    NOVEDAD_EN_BODEGA,
    
    /**
     * Paquete en tránsito hacia destino
     */
    EN_TRANSITO,
    
    /**
     * Paquete entregado al destinatario
     */
    ENTREGADO;
    
    /**
     * Determina si este estado es posterior al estado dado.
     * Útil para validar transiciones permitidas.
     * 
     * @param otro Estado a comparar
     * @return true si este estado viene después del estado dado
     */
    public boolean esPosteriorA(EstadoPaquete otro) {
        return this.ordinal() > otro.ordinal();
    }
    
    /**
     * Determina si este estado es anterior al estado dado.
     * 
     * @param otro Estado a comparar
     * @return true si este estado viene antes del estado dado
     */
    public boolean esAnteriorA(EstadoPaquete otro) {
        return this.ordinal() < otro.ordinal();
    }
}

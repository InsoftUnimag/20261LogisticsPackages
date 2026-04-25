package com.logistics.packages.domain.service;

import com.logistics.packages.domain.exception.TransicionInvalidaException;
import com.logistics.packages.domain.valueobject.EstadoPaquete;

/**
 * Servicio de dominio que valida las transiciones de estado permitidas.
 * Implementa las reglas de negocio para cambios de estado de paquetes.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 * FR-006: Bloquear actualizaciones inválidas
 */
public class ValidadorTransicionEstado {
    
    /**
     * Valida si una transición de estado es permitida.
     * 
     * @param estadoActual Estado actual del paquete
     * @param estadoNuevo Estado al que se desea transicionar
     * @throws TransicionInvalidaException si la transición no está permitida
     */
    public void validarTransicion(EstadoPaquete estadoActual, EstadoPaquete estadoNuevo) {
        
        if (estadoActual == null) {
            throw new IllegalArgumentException("El estado actual no puede ser nulo");
        }
        
        if (estadoNuevo == null) {
            throw new IllegalArgumentException("El estado nuevo no puede ser nulo");
        }
        
        // Regla: No se permite cambiar a NOVEDAD_EN_BODEGA desde estados posteriores a LISTO_PARA_DESPACHO
        if (estadoNuevo == EstadoPaquete.NOVEDAD_EN_BODEGA) {
            if (estadoActual.esPosteriorA(EstadoPaquete.LISTO_PARA_DESPACHO)) {
                throw new TransicionInvalidaException(
                    String.format("No se permite cambiar a NOVEDAD_EN_BODEGA desde el estado %s. " +
                                "Solo se permite desde estados anteriores a EN_TRANSITO.",
                                estadoActual)
                );
            }
        }
        
        // Regla: No se permite retroceder a estados anteriores (excepto NOVEDAD_EN_BODEGA)
        if (estadoNuevo != EstadoPaquete.NOVEDAD_EN_BODEGA && estadoNuevo.esAnteriorA(estadoActual)) {
            throw new TransicionInvalidaException(
                String.format("No se permite retroceder del estado %s al estado %s",
                            estadoActual, estadoNuevo)
            );
        }
        
        // Validar transiciones específicas según el flujo normal
        validarFlujoNormal(estadoActual, estadoNuevo);
    }
    
    /**
     * Valida transiciones según el flujo normal de estados.
     */
    private void validarFlujoNormal(EstadoPaquete estadoActual, EstadoPaquete estadoNuevo) {
        
        switch (estadoActual) {
            case RECIBIDO_EN_SEDE:
                if (estadoNuevo != EstadoPaquete.EN_CLASIFICACION && 
                    estadoNuevo != EstadoPaquete.NOVEDAD_EN_BODEGA) {
                    throw new TransicionInvalidaException(
                        "Desde RECIBIDO_EN_SEDE solo se puede pasar a EN_CLASIFICACION o NOVEDAD_EN_BODEGA"
                    );
                }
                break;
                
            case EN_CLASIFICACION:
                if (estadoNuevo != EstadoPaquete.LISTO_PARA_DESPACHO && 
                    estadoNuevo != EstadoPaquete.NOVEDAD_EN_BODEGA) {
                    throw new TransicionInvalidaException(
                        "Desde EN_CLASIFICACION solo se puede pasar a LISTO_PARA_DESPACHO o NOVEDAD_EN_BODEGA"
                    );
                }
                break;
                
            case LISTO_PARA_DESPACHO:
                if (estadoNuevo != EstadoPaquete.EN_TRANSITO && 
                    estadoNuevo != EstadoPaquete.NOVEDAD_EN_BODEGA) {
                    throw new TransicionInvalidaException(
                        "Desde LISTO_PARA_DESPACHO solo se puede pasar a EN_TRANSITO o NOVEDAD_EN_BODEGA"
                    );
                }
                break;
                
            case NOVEDAD_EN_BODEGA:
                // Desde novedad se puede resolver y continuar el flujo normal
                if (estadoNuevo != EstadoPaquete.EN_CLASIFICACION && 
                    estadoNuevo != EstadoPaquete.LISTO_PARA_DESPACHO) {
                    throw new TransicionInvalidaException(
                        "Desde NOVEDAD_EN_BODEGA solo se puede pasar a EN_CLASIFICACION o LISTO_PARA_DESPACHO " +
                        "después de resolver la novedad"
                    );
                }
                break;
                
            case EN_TRANSITO:
                if (estadoNuevo != EstadoPaquete.ENTREGADO) {
                    throw new TransicionInvalidaException(
                        "Desde EN_TRANSITO solo se puede pasar a ENTREGADO"
                    );
                }
                break;
                
            case ENTREGADO:
                throw new TransicionInvalidaException(
                    "No se puede cambiar el estado de un paquete ya ENTREGADO"
                );
        }
    }
    
    /**
     * Verifica si un paquete en el estado dado puede registrar una novedad.
     * 
     * @param estadoActual Estado actual del paquete
     * @return true si puede registrar novedad
     */
    public boolean puedeRegistrarNovedad(EstadoPaquete estadoActual) {
        return !estadoActual.esPosteriorA(EstadoPaquete.LISTO_PARA_DESPACHO);
    }
}

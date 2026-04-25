package com.logistics.packages.application.ports;

import java.util.UUID;

/**
 * Puerto de salida para publicar eventos relacionados con la clasificación de paquetes.
 * FR-009: Invoca automáticamente el siguiente paso de clasificación cuando el paquete
 * está listo para ser procesado.
 */
public interface ClasificacionEventPublisher {
    
    /**
     * Publica un evento indicando que un paquete está listo para ser clasificado por zona de destino.
     * Este evento será consumido por el caso de uso MOD1-UC-005 (Clasificar Paquete por Zona de Destino).
     * 
     * @param paqueteId El ID del paquete que está listo para clasificación
     */
    void publicarPaqueteListoParaClasificar(UUID paqueteId);
}

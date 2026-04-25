package com.logistics.packages.domain.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta asignar un paquete a una zona que ha alcanzado su capacidad máxima.
 * FR-008: Zona saturada al alcanzar límites configurados
 */
public class ZonaSaturadaException extends RuntimeException {
    
    private final UUID zonaId;
    private final String nombreZona;
    private final String tipoCapacidadExcedida;
    private final UUID zonaContingenciaId;
    
    public ZonaSaturadaException(UUID zonaId, String nombreZona, String tipoCapacidadExcedida, UUID zonaContingenciaId) {
        super(String.format("La zona '%s' ha alcanzado su capacidad máxima de %s. Se sugiere la zona de contingencia.", 
            nombreZona, tipoCapacidadExcedida));
        this.zonaId = zonaId;
        this.nombreZona = nombreZona;
        this.tipoCapacidadExcedida = tipoCapacidadExcedida;
        this.zonaContingenciaId = zonaContingenciaId;
    }
    
    public UUID getZonaId() {
        return zonaId;
    }
    
    public String getNombreZona() {
        return nombreZona;
    }
    
    public String getTipoCapacidadExcedida() {
        return tipoCapacidadExcedida;
    }
    
    public UUID getZonaContingenciaId() {
        return zonaContingenciaId;
    }
}

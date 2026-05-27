package com.logistics.packages.infrastructure.adapter.persistence.sede;

import com.logistics.packages.domain.model.Sede;
import com.logistics.packages.domain.valueobject.Direccion;
import lombok.experimental.UtilityClass;

/**
 * Mapeador entre la entidad JPA (SedeDbo) y el modelo de dominio (Sede).
 */
@UtilityClass
public class SedeMapper {
    
    /**
     * Convierte una entidad JPA a modelo de dominio.
     * @param dbo Entidad JPA
     * @return Modelo de dominio
     */
    public static Sede toDomain(SedeDbo dbo) {
        if (dbo == null) return null;
        
        Direccion direccion = new Direccion(
                dbo.getDireccion(),
                dbo.getCiudad(),
                dbo.getDepartamento(),
                dbo.getPais()
        );
        
        return Sede.builder()
                .id(dbo.getId())
                .nombre(dbo.getNombre())
                .direccion(direccion)
                .tipo(dbo.getTipo())
                .capacidadMaximaPeso(dbo.getCapacidadMaximaPeso())
                .capacidadMaximaVolumen(dbo.getCapacidadMaximaVolumen())
                .tarifaBase(dbo.getTarifaBase())
                .tarifaPorKg(dbo.getTarifaPorKg())
                .tarifaPorKm(dbo.getTarifaPorKm())
                .metodosPagoHabilitados(dbo.getMetodosPagoHabilitados() != null 
                        ? java.util.Arrays.asList(dbo.getMetodosPagoHabilitados()) 
                        : null)
                .build();
    }
    
    /**
     * Convierte un modelo de dominio a entidad JPA.
     * @param sede Modelo de dominio
     * @return Entidad JPA
     */
    public static SedeDbo toDbo(Sede sede) {
        if (sede == null) return null;
        
        return SedeDbo.builder()
                .id(sede.getId())
                .nombre(sede.getNombre())
                .direccion(sede.getDireccion().getDireccion())
                .ciudad(sede.getDireccion().getCiudad())
                .departamento(sede.getDireccion().getDepartamento())
                .pais(sede.getDireccion().getPais())
                .tipo(sede.getTipo())
                .capacidadMaximaPeso(sede.getCapacidadMaximaPeso())
                .capacidadMaximaVolumen(sede.getCapacidadMaximaVolumen())
                .tarifaBase(sede.getTarifaBase())
                .tarifaPorKg(sede.getTarifaPorKg())
                .tarifaPorKm(sede.getTarifaPorKm())
                .metodosPagoHabilitados(sede.getMetodosPagoHabilitados() != null 
                        ? sede.getMetodosPagoHabilitados().toArray(new com.logistics.packages.domain.valueobject.MetodoPago[0])
                        : null)
                .build();
    }
}

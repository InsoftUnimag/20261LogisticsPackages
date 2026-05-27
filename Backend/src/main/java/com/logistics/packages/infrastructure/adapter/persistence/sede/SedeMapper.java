package com.logistics.packages.infrastructure.adapter.persistence.sede;

import com.logistics.packages.domain.model.Sede;
import com.logistics.packages.domain.valueobject.Direccion;
import com.logistics.packages.domain.valueobject.MetodoPago;
import org.mapstruct.Mapper;

import java.util.Arrays;
import java.util.List;

/**
 * Mapper MapStruct para convertir entre SedeDbo (JPA) y Sede (dominio).
 */
@Mapper(componentModel = "spring")
public abstract class SedeMapper {

    public Sede toDomain(SedeDbo sedeDbo) {
        if (sedeDbo == null) {
            return null;
        }
        
        return Sede.builder()
                .id(sedeDbo.getId())
                .nombre(sedeDbo.getNombre())
                .direccion(construirDireccion(sedeDbo.getDireccion(), sedeDbo.getCiudad(), sedeDbo.getDepartamento(), sedeDbo.getPais()))
                .tipo(sedeDbo.getTipo())
                .capacidadMaximaPeso(sedeDbo.getCapacidadMaximaPeso())
                .capacidadMaximaVolumen(sedeDbo.getCapacidadMaximaVolumen())
                .tarifaBase(sedeDbo.getTarifaBase())
                .tarifaPorKg(sedeDbo.getTarifaPorKg())
                .tarifaPorKm(sedeDbo.getTarifaPorKm())
                .metodosPagoHabilitados(parseMetodosPago(sedeDbo.getMetodosPagoHabilitados()))
                .build();
    }

    public SedeDbo toDbo(Sede sede) {
        if (sede == null) {
            return null;
        }
        
        SedeDbo dbo = new SedeDbo();
        dbo.setId(sede.getId());
        dbo.setNombre(sede.getNombre());
        dbo.setTipo(sede.getTipo());
        dbo.setCapacidadMaximaPeso(sede.getCapacidadMaximaPeso());
        dbo.setCapacidadMaximaVolumen(sede.getCapacidadMaximaVolumen());
        dbo.setTarifaBase(sede.getTarifaBase());
        dbo.setTarifaPorKg(sede.getTarifaPorKg());
        dbo.setTarifaPorKm(sede.getTarifaPorKm());
        dbo.setMetodosPagoHabilitados(formatMetodosPago(sede.getMetodosPagoHabilitados()));
        
        if (sede.getDireccion() != null) {
            dbo.setDireccion(sede.getDireccion().getDireccion());
            dbo.setCiudad(sede.getDireccion().getCiudad());
            dbo.setDepartamento(sede.getDireccion().getDepartamento());
            dbo.setPais(sede.getDireccion().getPais());
        }
        
        return dbo;
    }

    /**
     * Convierte array de strings a lista de MetodoPago.
     */
    protected List<MetodoPago> parseMetodosPago(String[] methods) {
        if (methods == null || methods.length == 0) {
            return List.of();
        }
        return Arrays.stream(methods)
                .map(MetodoPago::valueOf)
                .toList();
    }

    /**
     * Convierte lista de MetodoPago a array de strings.
     */
    protected String[] formatMetodosPago(List<MetodoPago> metodos) {
        if (metodos == null || metodos.isEmpty()) {
            return new String[]{};
        }
        return metodos.stream()
                .map(MetodoPago::name)
                .toArray(String[]::new);
    }

    /**
     * Construye un Value Object Direccion a partir de los campos de la entidad JPA.
     */
    protected Direccion construirDireccion(String direccion, String ciudad, String departamento, String pais) {
        if (direccion == null && ciudad == null && departamento == null && pais == null) {
            return null;
        }
        return new Direccion(
                direccion != null ? direccion : "",
                ciudad != null ? ciudad : "",
                departamento != null ? departamento : "",
                pais != null ? pais : ""
        );
    }
}

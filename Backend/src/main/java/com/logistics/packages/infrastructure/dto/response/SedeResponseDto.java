package com.logistics.packages.infrastructure.dto.response;

import com.logistics.packages.domain.model.Sede;
import com.logistics.packages.domain.valueobject.MetodoPago;
import com.logistics.packages.domain.valueobject.TipoSede;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para consulta de sedes.
 * Retorna la información de una sede disponible para que el operador pueda seleccionar.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SedeResponseDto {
    
    private UUID id;
    private String nombre;
    private String direccion;
    private String ciudad;
    private String departamento;
    private TipoSede tipo;
    private BigDecimal capacidadMaximaPeso;
    private BigDecimal capacidadMaximaVolumen;
    private BigDecimal tarifaBase;
    private BigDecimal tarifaPorKg;
    private BigDecimal tarifaPorKm;
    private List<MetodoPago> metodosPagoHabilitados;
    
    /**
     * Convierte un modelo de dominio (Sede) a DTO de respuesta.
     * @param sede Modelo de dominio
     * @return DTO de respuesta
     */
    public static SedeResponseDto fromDomain(Sede sede) {
        return SedeResponseDto.builder()
                .id(sede.getId())
                .nombre(sede.getNombre())
                .direccion(sede.getDireccion().getDireccion())
                .ciudad(sede.getDireccion().getCiudad())
                .departamento(sede.getDireccion().getDepartamento())
                .tipo(sede.getTipo())
                .capacidadMaximaPeso(sede.getCapacidadMaximaPeso())
                .capacidadMaximaVolumen(sede.getCapacidadMaximaVolumen())
                .tarifaBase(sede.getTarifaBase())
                .tarifaPorKg(sede.getTarifaPorKg())
                .tarifaPorKm(sede.getTarifaPorKm())
                .metodosPagoHabilitados(sede.getMetodosPagoHabilitados())
                .build();
    }
}

package com.logistics.packages.infrastructure.adapter.persistence.paquete;

import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.Dimensiones;
import com.logistics.packages.domain.valueobject.PrecioEnvio;
import com.logistics.packages.domain.valueobject.Peso;
import com.logistics.packages.infrastructure.adapter.persistence.persona.PersonaMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {PersonaMapper.class})
public interface PaqueteMapper {

    @Mapping(source = "direccionDestino", target = "direccionDestino")
    @Mapping(source = "coordenadas.latitud", target = "latitud")
    @Mapping(source = "coordenadas.longitud", target = "longitud")
    @Mapping(source = "precioEnvio", target = "precioEnvio", qualifiedByName = "precioToBigDecimal")
    @Mapping(source = "peso.kilogramos", target = "peso")
    @Mapping(source = "dimensiones.largoCm", target = "largo")
    @Mapping(source = "dimensiones.anchoCm", target = "ancho")
    @Mapping(source = "dimensiones.altoCm", target = "alto")
    @Mapping(source = "urlEvidenciaEntrega", target = "urlEvidenciaEntrega")
    @Mapping(source = "nombreFirmante", target = "nombreFirmante")
    @Mapping(source = "fechaEntregaUtc", target = "fechaEntregaUtc")
    @Mapping(source = "alertaCargaEspecial", target = "alertaCargaEspecial")
    @Mapping(source = "alertaDensidadAtipica", target = "alertaDensidadAtipica")
    @Mapping(source = "etiquetaDigital", target = "etiquetaDigital")
    PaqueteDbo toDbo(Paquete domain);

    @Mapping(target = "direccionDestino", source = "direccionDestino")
    @Mapping(target = "coordenadas.latitud", source = "latitud")
    @Mapping(target = "coordenadas.longitud", source = "longitud")
    @Mapping(target = "precioEnvio", source = "precioEnvio", qualifiedByName = "bigDecimalToPrecio")
    @Mapping(target = "peso.kilogramos", source = "peso")
    @Mapping(target = "dimensiones.largoCm", source = "largo")
    @Mapping(target = "dimensiones.anchoCm", source = "ancho")
    @Mapping(target = "dimensiones.altoCm", source = "alto")
    @Mapping(target = "urlEvidenciaEntrega", source = "urlEvidenciaEntrega")
    @Mapping(target = "nombreFirmante", source = "nombreFirmante")
    @Mapping(target = "fechaEntregaUtc", source = "fechaEntregaUtc")
    @Mapping(target = "alertaCargaEspecial", source = "alertaCargaEspecial")
    @Mapping(target = "alertaDensidadAtipica", source = "alertaDensidadAtipica")
    @Mapping(target = "etiquetaDigital", source = "etiquetaDigital")
    Paquete toDomain(PaqueteDbo dbo);

    @Named("precioToBigDecimal")
    default BigDecimal mapToBigDecimal(PrecioEnvio precio) {
        if (precio == null) {
            return null;
        }
        return precio.getValor();
    }

    @Named("bigDecimalToPrecio")
    default PrecioEnvio mapToPrecio(BigDecimal precio) {
        if (precio == null) {
            return null;
        }
        return new PrecioEnvio(precio);
    }

    @Named("doubleToPeso")
    default Peso mapToPeso(Double peso) {
        if (peso == null) {
            return null;
        }
        return new Peso(peso);
    }

    @Named("pesoToDouble")
    default Double mapPesoToDouble(Peso peso) {
        if (peso == null) {
            return null;
        }
        return peso.getKilogramos();
    }

    @Named("toDimensiones")
    default Dimensiones mapToDimensiones(Double largo, Double ancho, Double alto) {
        if (largo == null || ancho == null || alto == null) {
            return null;
        }
        return new Dimensiones(largo, ancho, alto);
    }
}
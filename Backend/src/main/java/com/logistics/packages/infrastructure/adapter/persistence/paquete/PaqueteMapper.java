package com.logistics.packages.infrastructure.adapter.persistence.paquete;

import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.Direccion;
import com.logistics.packages.domain.valueobject.PrecioEnvio;
import com.logistics.packages.infrastructure.adapter.persistence.persona.PersonaMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {PersonaMapper.class})
public interface PaqueteMapper {

    @Mapping(source = "direccionDestino", target = "direccionDestino", qualifiedByName = "direccionToString")
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

    @Mapping(target = "direccionDestino", source = "direccionDestino", qualifiedByName = "stringToDireccion")
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

    @Named("direccionToString")
    default String map(Direccion direccion) {
        if (direccion == null) {
            return null;
        }
        return String.join(", ", direccion.getDireccion(), direccion.getCiudad(), direccion.getDepartamento(), direccion.getPais());
    }

    @Named("stringToDireccion")
    default Direccion map(String direccion) {
        if (direccion == null || direccion.isEmpty()) {
            return null;
        }
        String[] parts = direccion.split(", ");
        if (parts.length < 4) {
            // Handle cases where the string is not in the expected format
            return new Direccion(direccion, null, null, null);
        }
        return new Direccion(parts[0], parts[1], parts[2], parts[3]);
    }

    @Named("precioToBigDecimal")
    default BigDecimal map(PrecioEnvio precio) {
        if (precio == null) {
            return null;
        }
        return precio.getValor();
    }

    @Named("bigDecimalToPrecio")
    default PrecioEnvio map(BigDecimal precio) {
        if (precio == null) {
            return null;
        }
        return new PrecioEnvio(precio);
    }
}

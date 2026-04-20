package com.logistics.packages.infrastructure.adapter.out.persistence;

import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.Direccion;
import com.logistics.packages.domain.valueobject.PrecioEnvio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface PaqueteMapper {

    @Mapping(source = "direccionDestino", target = "direccionDestino", qualifiedByName = "direccionToString")
    @Mapping(source = "coordenadas.latitud", target = "latitud")
    @Mapping(source = "coordenadas.longitud", target = "longitud")
    @Mapping(source = "remitente.numeroDocumento", target = "remitenteDocumento")
    @Mapping(source = "remitente.nombreCompleto", target = "remitenteNombre")
    @Mapping(source = "remitente.telefono", target = "remitenteTelefono")
    @Mapping(source = "destinatario.numeroDocumento", target = "destinatarioDocumento")
    @Mapping(source = "destinatario.nombreCompleto", target = "destinatarioNombre")
    @Mapping(source = "destinatario.telefono", target = "destinatarioTelefono")
    @Mapping(source = "precioEnvio", target = "precioEnvio", qualifiedByName = "precioToBigDecimal")
    PaqueteDbo toDbo(Paquete domain);

    @Mapping(target = "direccionDestino", source = "direccionDestino", qualifiedByName = "stringToDireccion")
    @Mapping(target = "coordenadas.latitud", source = "latitud")
    @Mapping(target = "coordenadas.longitud", source = "longitud")
    @Mapping(target = "remitente.numeroDocumento", source = "remitenteDocumento")
    @Mapping(target = "remitente.nombreCompleto", source = "remitenteNombre")
    @Mapping(target = "remitente.telefono", source = "remitenteTelefono")
    @Mapping(target = "destinatario.numeroDocumento", source = "destinatarioDocumento")
    @Mapping(target = "destinatario.nombreCompleto", source = "destinatarioNombre")
    @Mapping(target = "destinatario.telefono", source = "destinatarioTelefono")
    @Mapping(target = "precioEnvio", source = "precioEnvio", qualifiedByName = "bigDecimalToPrecio")
    @Mapping(target = "alertaCargaEspecial", ignore = true)
    @Mapping(target = "alertaDensidadAtipica", ignore = true)
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

package com.logistics.packages.infrastructure.adapter.persistence.paquete;

import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.model.Persona;
import com.logistics.packages.domain.valueobject.*;
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

    default Paquete toDomain(PaqueteDbo dbo) {
        if (dbo == null) return null;

        Persona remitente = null;
        if (dbo.getRemitente() != null) {
            remitente = Persona.builder()
                    .tipoDocumento(dbo.getRemitente().getTipoDocumento())
                    .numeroDocumento(dbo.getRemitente().getNumeroDocumento())
                    .nombreCompleto(dbo.getRemitente().getNombreCompleto())
                    .telefono(dbo.getRemitente().getTelefono())
                    .correoElectronico(dbo.getRemitente().getCorreoElectronico())
                    .direccion(dbo.getDireccionDestino())
                    .build();
        }

        Persona destinatario = null;
        if (dbo.getDestinatario() != null) {
            destinatario = Persona.builder()
                    .tipoDocumento(dbo.getDestinatario().getTipoDocumento())
                    .numeroDocumento(dbo.getDestinatario().getNumeroDocumento())
                    .nombreCompleto(dbo.getDestinatario().getNombreCompleto())
                    .telefono(dbo.getDestinatario().getTelefono())
                    .correoElectronico(dbo.getDestinatario().getCorreoElectronico())
                    .build();
        }

        Coordenadas coordenadas = dbo.getLatitud() != null && dbo.getLongitud() != null
                ? new Coordenadas(dbo.getLatitud(), dbo.getLongitud())
                : null;

        Peso peso = dbo.getPeso() != null ? new Peso(dbo.getPeso()) : null;

        Dimensiones dimensiones = dbo.getLargo() != null && dbo.getAncho() != null && dbo.getAlto() != null
                ? new Dimensiones(dbo.getLargo(), dbo.getAncho(), dbo.getAlto())
                : null;

        PrecioEnvio precioEnvio = dbo.getPrecioEnvio() != null ? new PrecioEnvio(dbo.getPrecioEnvio()) : null;

        return Paquete.reconstruir(
                dbo.getId(), dbo.getFechaIngresoUtc(), dbo.getEstado(),
                dbo.getSedeId(), dbo.getDireccionDestino(), coordenadas,
                dbo.getEstadoGps(), dbo.getValorDeclarado(), dbo.getMetodoPago(),
                remitente, destinatario,
                peso, dimensiones, dbo.getVolumenM3(),
                dbo.getPesoVolumetrico(), dbo.getPesoFacturable(),
                dbo.getTipoMercancia(), dbo.getCategoriaCarga(),
                dbo.getIndicadorFormaIrregular(), precioEnvio,
                dbo.getDistanciaEstimadaKm(), dbo.getRutaId(),
                dbo.getZonaAlmacenamientoId(), dbo.getZonaDestinoId(),
                dbo.isAlertaCargaEspecial(), dbo.isAlertaDensidadAtipica(),
                dbo.getUrlEvidenciaEntrega(), dbo.getNombreFirmante(),
                dbo.getFechaEntregaUtc(), dbo.getEtiquetaDigital()
        );
    }

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

package com.logistics.packages.infrastructure.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.TipoMercancia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Payload JSON para la solicitud de ruta al Módulo de Gestión de Rutas
 * FR-002: Contiene UUID del paquete, peso, volumen, tipo de mercancía, 
 * dirección de destino y coordenadas GPS
 * MOD1-IP-003 - Phase 3
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRutaPayload {

    @JsonProperty("paquete_id")
    private UUID paqueteId;

    @JsonProperty("peso_kg")
    private Double pesoKg;

    @JsonProperty("volumen_m3")
    private Double volumenM3;

    @JsonProperty("tipo_mercancia")
    private TipoMercancia tipoMercancia;

    @JsonProperty("direccion_destino")
    private String direccionDestino;

    @JsonProperty("latitud")
    private Double latitud;

    @JsonProperty("longitud")
    private Double longitud;

    @JsonProperty("peso_facturable")
    private Double pesoFacturable;

    @JsonProperty("categoria_carga")
    private String categoriaCarga;

    /**
     * Construye el payload desde una entidad Paquete
     * 
     * @param paquete El paquete del cual extraer los datos
     * @return El payload construido
     */
    public static SolicitudRutaPayload from(Paquete paquete) {
        return SolicitudRutaPayload.builder()
                .paqueteId(paquete.getId())
                .pesoKg(paquete.getPeso() != null ? paquete.getPeso().getKilogramos() : null)
                .volumenM3(paquete.getVolumenM3())
                .tipoMercancia(paquete.getTipoMercancia())
                .direccionDestino(buildDireccionCompleta(paquete))
                .latitud(paquete.getCoordenadas() != null ? paquete.getCoordenadas().latitud() : null)
                .longitud(paquete.getCoordenadas() != null ? paquete.getCoordenadas().longitud() : null)
                .pesoFacturable(paquete.getPesoFacturable())
                .categoriaCarga(paquete.getCategoriaCarga() != null ? paquete.getCategoriaCarga().name() : null)
                .build();
    }

    private static String buildDireccionCompleta(Paquete paquete) {
        if (paquete.getDireccionDestino() == null) {
            return null;
        }
        var direccion = paquete.getDireccionDestino();
        return String.format("%s, %s, %s, %s",
                direccion.getDireccion(),
                direccion.getCiudad(),
                direccion.getDepartamento(),
                direccion.getPais());
    }
}

package com.logistics.packages.infrastructure.adapter.out.persistence;

import com.logistics.packages.domain.valueobject.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "paquetes")
@Getter
@Setter
public class PaqueteDbo {
    @Id
    private UUID id;
    private LocalDateTime fechaIngresoUtc;
    @Enumerated(EnumType.STRING)
    private EstadoPaquete estado;
    private String sedeId;
    private String direccionDestino;
    private Double latitud;
    private Double longitud;
    @Enumerated(EnumType.STRING)
    private EstadoGps estadoGps;
    private BigDecimal valorDeclarado;
    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;
    private String remitenteDocumento;
    private String remitenteNombre;
    private String remitenteTelefono;
    private String destinatarioDocumento;
    private String destinatarioNombre;
    private String destinatarioTelefono;
    private Double peso;
    private Double largo;
    private Double ancho;
    private Double alto;
    private Double volumenM3;
    private Double pesoVolumetrico;
    private Double pesoFacturable;
    @Enumerated(EnumType.STRING)
    private TipoMercancia tipoMercancia;
    @Enumerated(EnumType.STRING)
    private CategoriaCarga categoriaCarga;
    private Boolean indicadorFormaIrregular;
    private BigDecimal precioEnvio;
    private Double distanciaEstimadaKm;
    private UUID rutaId;
    private UUID zonaAlmacenamientoId;
    private UUID zonaDestinoId;
}

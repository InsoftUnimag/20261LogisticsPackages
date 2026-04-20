package com.logistics.packages.infrastructure.adapter.out.persistence.paquete;

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

    @Column(name = "fecha_ingreso_utc")
    private LocalDateTime fechaIngresoUtc;

    @Enumerated(EnumType.STRING)
    private EstadoPaquete estado;

    @Column(name = "sede_id")
    private String sedeId;

    @Column(name = "direccion_destino")
    private String direccionDestino;

    private Double latitud;
    private Double longitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_gps")
    private EstadoGps estadoGps;

    @Column(name = "valor_declarado")
    private BigDecimal valorDeclarado;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPago metodoPago;

    @Column(name = "remitente_documento")
    private String remitenteDocumento;

    @Column(name = "remitente_nombre")
    private String remitenteNombre;

    @Column(name = "remitente_telefono")
    private String remitenteTelefono;

    @Column(name = "destinatario_documento")
    private String destinatarioDocumento;

    @Column(name = "destinatario_nombre")
    private String destinatarioNombre;

    @Column(name = "destinatario_telefono")
    private String destinatarioTelefono;

    private Double peso;
    private Double largo;
    private Double ancho;
    private Double alto;

    @Column(name = "volumen_m3")
    private Double volumenM3;

    @Column(name = "peso_volumetrico")
    private Double pesoVolumetrico;

    @Column(name = "peso_facturable")
    private Double pesoFacturable;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_mercancia")
    private TipoMercancia tipoMercancia;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_carga")
    private CategoriaCarga categoriaCarga;

    @Column(name = "indicador_forma_irregular")
    private Boolean indicadorFormaIrregular;

    @Column(name = "precio_envio")
    private BigDecimal precioEnvio;

    @Column(name = "distancia_estimada_km")
    private Double distanciaEstimadaKm;

    @Column(name = "ruta_id")
    private UUID rutaId;

    @Column(name = "zona_almacenamiento_id")
    private UUID zonaAlmacenamientoId;

    @Column(name = "zona_destino_id")
    private UUID zonaDestinoId;
}

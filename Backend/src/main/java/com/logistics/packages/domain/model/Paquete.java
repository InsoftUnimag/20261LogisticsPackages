package com.logistics.packages.domain.model;

import com.logistics.packages.domain.valueobject.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paquete {
    private UUID id;
    private LocalDateTime fechaIngresoUtc;
    private EstadoPaquete estado;
    private String sedeId;
    private Direccion direccionDestino;
    private Coordenadas coordenadas;
    private EstadoGps estadoGps;
    private BigDecimal valorDeclarado;
    private MetodoPago metodoPago;
    private Persona remitente;
    private Persona destinatario;
    private Double peso;
    private Double largo;
    private Double ancho;
    private Double alto;
    private Double volumenM3;
    private Double pesoVolumetrico;
    private Double pesoFacturable;
    private TipoMercancia tipoMercancia;
    private CategoriaCarga categoriaCarga;
    private Boolean indicadorFormaIrregular;
    private PrecioEnvio precioEnvio;
    private Double distanciaEstimadaKm;
    private UUID rutaId;
    private UUID zonaAlmacenamientoId;
    private UUID zonaDestinoId;

    @Builder.Default
    private boolean alertaCargaEspecial = false;
    @Builder.Default
    private boolean alertaDensidadAtipica = false;

    public void prePersist() {
        this.id = UUID.randomUUID();
        this.fechaIngresoUtc = LocalDateTime.now(ZoneOffset.UTC);
        this.estado = EstadoPaquete.RECIBIDO_EN_SEDE;
        this.estadoGps = EstadoGps.PENDIENTE;
    }

    public void asignarCoordenadas(Coordenadas coordenadas) {
        this.coordenadas = coordenadas;
        this.estadoGps = EstadoGps.RESUELTO;
    }

    public void procesarPesaje(Double peso, Double largo, Double ancho, Double alto, double factorConversion) {
        if (peso == null || peso <= 0 || peso > 70 || largo == null || largo <= 0 || ancho == null || ancho <= 0 || alto == null || alto <= 0) {
            throw new IllegalArgumentException("Las dimensiones y el peso deben ser valores positivos y el peso no debe exceder los 70 kg.");
        }
        this.peso = peso;
        this.largo = largo;
        this.ancho = ancho;
        this.alto = alto;
        this.calcularVolumen();
        this.calcularPesoVolumetrico(factorConversion);
        this.calcularPesoFacturable();
        this.determinarCategoriaCarga();
        this.verificarDensidadAtipica();
    }

    public void calcularVolumen() {
        if (this.largo != null && this.ancho != null && this.alto != null) {
            this.volumenM3 = (this.largo * this.ancho * this.alto) / 1_000_000.0;
        }
    }

    public void calcularPesoVolumetrico(double factorConversion) {
        if (this.volumenM3 != null) {
            this.pesoVolumetrico = this.volumenM3 * factorConversion;
        }
    }

    public void calcularPesoFacturable() {
        if (this.peso != null && this.pesoVolumetrico != null) {
            this.pesoFacturable = Math.max(this.peso, this.pesoVolumetrico);
        }
    }

    private void determinarCategoriaCarga() {
        if ((peso > 50 && peso <= 70) || (this.volumenM3 != null && this.volumenM3 > 0.5 && this.volumenM3 <= 0.7)) {
            this.categoriaCarga = CategoriaCarga.CARGA_ESPECIAL;
            this.alertaCargaEspecial = true;
        } else {
            this.categoriaCarga = CategoriaCarga.NORMAL;
        }
    }

    private void verificarDensidadAtipica() {
        if (this.peso != null && this.pesoVolumetrico != null) {
            double diferencia = Math.abs(this.peso - this.pesoVolumetrico);
            double porcentajeDiferencia = (diferencia / this.peso) * 100;
            if (porcentajeDiferencia > 30) {
                this.alertaDensidadAtipica = true;
            }
        }
    }

    public void calcularPrecioEnvio(BigDecimal tarifaBase, BigDecimal tarifaPorKg, BigDecimal tarifaPorKm, BigDecimal recargoTipoMercancia, BigDecimal recargoCategoriaCarga) {
        BigDecimal precio = tarifaBase;
        precio = precio.add(new BigDecimal(this.pesoFacturable).multiply(tarifaPorKg));
        precio = precio.add(new BigDecimal(this.distanciaEstimadaKm).multiply(tarifaPorKm));
        precio = precio.add(recargoTipoMercancia);
        precio = precio.add(recargoCategoriaCarga);
        this.precioEnvio = new PrecioEnvio(precio);
    }

    public void cambiarEstado(EstadoPaquete nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public void asignarRuta(UUID rutaId) {
        this.rutaId = rutaId;
    }

    public void asignarZonaAlmacenamiento(UUID zonaAlmacenamientoId) {
        this.zonaAlmacenamientoId = zonaAlmacenamientoId;
        this.estado = EstadoPaquete.EN_CLASIFICACION;
    }

    public void asignarZonaDestino(UUID zonaDestinoId) {
        this.zonaDestinoId = zonaDestinoId;
    }
}

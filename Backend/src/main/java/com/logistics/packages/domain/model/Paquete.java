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
    
    // Atributos Físicos y Tarifarios (MOD1-UC-002)
    private Peso peso;
    private Dimensiones dimensiones;
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

    /**
     * Procesa el pesaje del paquete con los nuevos datos físicos.
     * FR-001, FR-003: Validaciones en los Value Objects Peso y Dimensiones
     * FR-004, FR-005, FR-006: Cálculos de volumen, peso volumétrico y facturable
     * 
     * @param peso Peso del paquete
     * @param dimensiones Dimensiones del paquete
     * @param tipoMercancia Tipo de mercancía
     * @param irregular Indicador de forma irregular
     */
    public void procesarPesaje(Peso peso, Dimensiones dimensiones, TipoMercancia tipoMercancia, boolean irregular) {
        this.peso = peso;
        this.dimensiones = dimensiones;
        this.tipoMercancia = tipoMercancia;
        this.indicadorFormaIrregular = irregular;

        // Cálculos según las reglas de negocio
        this.volumenM3 = calcularVolumen();
        this.pesoVolumetrico = calcularPesoVolumetrico();
        this.pesoFacturable = determinarPesoFacturable();
        this.categoriaCarga = determinarCategoriaCarga();
        verificarDensidadAtipica();
    }

    /**
     * FR-004: Calcula el volumen en metros cúbicos
     * Volumen = (L × A × H) / 1,000,000
     */
    private Double calcularVolumen() {
        if (this.dimensiones != null) {
            return this.dimensiones.calcularVolumenM3();
        }
        return null;
    }

    /**
     * FR-005: Calcula el peso volumétrico
     * Peso Volumétrico = Volumen (m³) × 250 kg/m³
     */
    private Double calcularPesoVolumetrico() {
        if (this.volumenM3 != null) {
            return this.volumenM3 * 250;
        }
        return null;
    }

    /**
     * FR-006: Determina el peso facturable
     * Peso Facturable = MAX(Peso Real, Peso Volumétrico)
     */
    private Double determinarPesoFacturable() {
        if (this.peso != null && this.pesoVolumetrico != null) {
            return Math.max(this.peso.getKilogramos(), this.pesoVolumetrico);
        }
        return null;
    }

    /**
     * FR-002: Determina la categoría de carga
     * Carga Especial si: Peso > 50 kg O Volumen > 0.5 m³
     */
    private CategoriaCarga determinarCategoriaCarga() {
        if (this.peso != null && this.peso.getKilogramos() > 50) {
            this.alertaCargaEspecial = true;
            return CategoriaCarga.CARGA_ESPECIAL;
        }
        if (this.volumenM3 != null && this.volumenM3 > 0.5) {
            this.alertaCargaEspecial = true;
            return CategoriaCarga.CARGA_ESPECIAL;
        }
        return CategoriaCarga.NORMAL;
    }

    /**
     * FR-010: Verifica si hay una densidad atípica
     * Densidad Atípica si: |Peso Real - Peso Volumétrico| / Peso Real > 30%
     */
    private void verificarDensidadAtipica() {
        if (this.peso != null && this.pesoVolumetrico != null) {
            double diferencia = Math.abs(this.peso.getKilogramos() - this.pesoVolumetrico);
            double porcentajeDiferencia = (diferencia / this.peso.getKilogramos());
            if (porcentajeDiferencia > 0.3) {
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

    /**
     * FR-003: Asigna una ruta al paquete y cambia su estado a LISTO_PARA_DESPACHO
     * T302 [P] - MOD1-IP-003: No permite reasignar si ya tiene ruta
     * 
     * @param rutaId El ID de la ruta asignada por el Módulo de Gestión de Rutas
     * @throws IllegalArgumentException si el rutaId es nulo
     * @throws IllegalStateException si el paquete ya tiene una ruta asignada
     */
    public void asignarRuta(UUID rutaId) {
        if (rutaId == null) {
            throw new IllegalArgumentException("El ID de ruta no puede ser nulo.");
        }
        if (this.rutaId != null) {
            throw new IllegalStateException("El paquete ya tiene una ruta asignada.");
        }
        this.rutaId = rutaId;
        this.estado = EstadoPaquete.LISTO_PARA_DESPACHO;
    }

    public void asignarZonaAlmacenamiento(UUID zonaAlmacenamientoId) {
        this.zonaAlmacenamientoId = zonaAlmacenamientoId;
        this.estado = EstadoPaquete.EN_CLASIFICACION;
    }

    public void asignarZonaDestino(UUID zonaDestinoId) {
        this.zonaDestinoId = zonaDestinoId;
    }
}

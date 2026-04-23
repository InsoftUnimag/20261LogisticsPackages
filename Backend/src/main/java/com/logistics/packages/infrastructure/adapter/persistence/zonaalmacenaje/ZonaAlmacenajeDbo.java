package com.logistics.packages.infrastructure.adapter.persistence.zonaalmacenaje;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "zonas_almacenaje")
@Getter
@Setter
public class ZonaAlmacenajeDbo {

    @Id
    private UUID id;

    private String nombre;

    @Column(unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria")
    private String categoria; // Storing as String for simplicity, could be enum

    @Column(name = "capacidad_max_kg")
    private BigDecimal capacidadMaxKg;

    @Column(name = "capacidad_max_m3")
    private BigDecimal capacidadMaxM3;

    @Column(name = "capacidad_max_paquetes")
    private Integer capacidadMaxPaquetes;

    @Column(name = "peso_actual_kg")
    private BigDecimal pesoActualKg;

    @Column(name = "volumen_actual_m3")
    private BigDecimal volumenActualM3;

    @Column(name = "contador_paquetes")
    private Integer contadorPaquetes;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private String estado; // Storing as String for simplicity, could be enum

    @Column(name = "ubicacion_fisica")
    private String ubicacionFisica;

    @Column(name = "id_sede")
    private UUID idSede;

    @Column(name = "zona_contingencia_id")
    private UUID zonaContingenciaId;
}

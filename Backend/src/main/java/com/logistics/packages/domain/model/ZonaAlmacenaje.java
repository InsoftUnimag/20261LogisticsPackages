package com.logistics.packages.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "zonas_almacenaje")
public class ZonaAlmacenaje {

    @Id
    private UUID id;
    private String nombre;
    @Column(unique = true)
    private String codigo;
    @Enumerated(EnumType.STRING)
    private CategoriaZona categoria;
    private BigDecimal capacidadMaxKg;
    private BigDecimal capacidadMaxM3;
    private Integer capacidadMaxPaquetes;
    private BigDecimal pesoActualKg;
    private BigDecimal volumenActualM3;
    private Integer contadorPaquetes;
    @Enumerated(EnumType.STRING)
    private EstadoZona estado;
    private String ubicacionFisica;
    private UUID idSede;
    private UUID zonaContingenciaId;

    public enum CategoriaZona {
        NORMAL,
        DELICADA,
        ALTO_RIESGO,
        RETENCION
    }

    public enum EstadoZona {
        DISPONIBLE,
        PARCIAL,
        SATURADO,
        BLOQUEADO
    }
}

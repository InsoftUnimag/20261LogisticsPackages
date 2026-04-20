package com.logistics.packages.domain.model;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZonaAlmacenaje {

    private UUID id;
    private String nombre;
    private String codigo;
    private CategoriaZona categoria;
    private BigDecimal capacidadMaxKg;
    private BigDecimal capacidadMaxM3;
    private Integer capacidadMaxPaquetes;
    private BigDecimal pesoActualKg;
    private BigDecimal volumenActualM3;
    private Integer contadorPaquetes;
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

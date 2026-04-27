package com.logistics.packages.infrastructure.adapter.persistence.zonaalmacenaje;

import com.logistics.packages.domain.valueobject.EstadoZona;
import com.logistics.packages.domain.valueobject.CategoriaZona;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @Column(name = "categoria", columnDefinition = "categoria_zona_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CategoriaZona categoria;

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
    @Column(name = "estado", columnDefinition = "estado_zona_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private EstadoZona estado;

    @Column(name = "ubicacion_fisica")
    private String ubicacionFisica;

    @Column(name = "id_sede")
    private UUID idSede;

    @Column(name = "zona_contingencia_id")
    private UUID zonaContingenciaId;
}

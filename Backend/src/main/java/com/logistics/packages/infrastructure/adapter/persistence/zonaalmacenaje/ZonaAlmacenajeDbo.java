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

/**
 * Entidad JPA para ZonaAlmacenaje.
 * MOD1-IP-004: B1 - Incluye @Version para bloqueo optimista (FIFO)
 */
@Entity
@Table(name = "zonas_almacenaje")
@Getter
@Setter
public class ZonaAlmacenajeDbo {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
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
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID idSede;

    @Column(name = "zona_contingencia_id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID zonaContingenciaId;

    /**
     * Versión para bloqueo optimista.
     * Previene que dos almacenistas actualicen la misma zona simultáneamente (FIFO).
     */
    @Version
    @Column(name = "version")
    private Long version;
}

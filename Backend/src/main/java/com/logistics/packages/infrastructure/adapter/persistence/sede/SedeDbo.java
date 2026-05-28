package com.logistics.packages.infrastructure.adapter.persistence.sede;

import com.logistics.packages.domain.valueobject.TipoSede;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para Sede.
 * Mapeo a la tabla 'sedes' en la base de datos.
 */
@Entity
@Table(name = "sedes")
@Getter
@Setter
public class SedeDbo {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    private String direccion;

    private String ciudad;

    private String departamento;

    private String pais;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "tipo_sede_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TipoSede tipo;

    @Column(name = "capacidad_maxima_peso")
    private BigDecimal capacidadMaximaPeso;

    @Column(name = "capacidad_maxima_volumen")
    private BigDecimal capacidadMaximaVolumen;

    @Column(name = "tarifa_base")
    private BigDecimal tarifaBase;

    @Column(name = "tarifa_por_kg")
    private BigDecimal tarifaPorKg;

    @Column(name = "tarifa_por_km")
    private BigDecimal tarifaPorKm;

    @Column(name = "metodos_pago_habilitados", columnDefinition = "text[]")
    private String[] metodosPagoHabilitados;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @Column(name = "fecha_creacion")
    private Instant fechaCreacion;
}

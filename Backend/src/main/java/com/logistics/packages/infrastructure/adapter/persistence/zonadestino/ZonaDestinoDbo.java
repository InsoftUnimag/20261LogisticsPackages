package com.logistics.packages.infrastructure.adapter.persistence.zonadestino;

import com.logistics.packages.domain.valueobject.CategoriaZona;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Entidad JPA para ZonaDestino
 * MOD1-IP-005
 */
@Entity
@Table(name = "zonas_destino")
@Getter
@Setter
public class ZonaDestinoDbo {

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

    @Column(name = "latitud_min")
    private Double latitudMin;

    @Column(name = "latitud_max")
    private Double latitudMax;

    @Column(name = "longitud_min")
    private Double longitudMin;

    @Column(name = "longitud_max")
    private Double longitudMax;

    @Column(name = "capacidad_max_paquetes")
    private Integer capacidadMaxPaquetes;

    @Column(name = "contador_paquetes")
    private Integer contadorPaquetes;

    @Column(name = "id_sede")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID idSede;
}

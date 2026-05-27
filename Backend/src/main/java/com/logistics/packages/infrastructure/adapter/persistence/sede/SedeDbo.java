package com.logistics.packages.infrastructure.adapter.persistence.sede;

import com.logistics.packages.domain.valueobject.MetodoPago;
import com.logistics.packages.domain.valueobject.TipoSede;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para la tabla "sedes".
 * Mapea las sedes de operación del sistema logístico.
 */
@Entity
@Table(name = "sedes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SedeDbo {
    
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 255)
    private String nombre;
    
    @Column(nullable = false, length = 500)
    private String direccion;
    
    @Column(nullable = false, length = 100)
    private String ciudad;
    
    @Column(nullable = false, length = 100)
    private String departamento;
    
    @Column(nullable = false, length = 100)
    private String pais;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, columnDefinition = "tipo_sede_enum")
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
    
    @Column(name = "metodos_pago_habilitados")
    private MetodoPago[] metodosPagoHabilitados;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}

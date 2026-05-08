package com.logistics.packages.infrastructure.adapter.persistence.persona;

import com.logistics.packages.domain.valueobject.Direccion;
import com.logistics.packages.domain.valueobject.TipoDocumento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "personas")
@Getter
@Setter
public class PersonaDbo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", columnDefinition = "tipo_documento_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", unique = true)
    private String numeroDocumento;

    @Column(name = "nombre_completo")
    private String nombreCompleto;

    private String telefono;

    @Column(name = "correo_electronico")
    private String correoElectronico;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "direccion", column = @Column(name = "direccion_linea")),
        @AttributeOverride(name = "ciudad", column = @Column(name = "ciudad")),
        @AttributeOverride(name = "departamento", column = @Column(name = "departamento")),
        @AttributeOverride(name = "pais", column = @Column(name = "pais"))
    })
    private Direccion direccion;
}

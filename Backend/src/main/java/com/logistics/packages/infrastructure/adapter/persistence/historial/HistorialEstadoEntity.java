package com.logistics.packages.infrastructure.adapter.persistence.historial;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para el historial de estados de paquetes.
 * MOD1-UC-006: Persistencia inmutable del historial de transiciones.
 */
@Entity
@Table(name = "historial_estados")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialEstadoEntity {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "paquete_id", nullable = false)
    private UUID paqueteId;
    
    @Column(name = "estado_anterior", nullable = false, length = 50)
    private String estadoAnterior;
    
    @Column(name = "estado_nuevo", nullable = false, length = 50)
    private String estadoNuevo;
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
    
    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;
    
    @Column(name = "url_evidencia", length = 500)
    private String urlEvidencia;
    
    @Column(name = "fecha_transicion_utc", nullable = false)
    private LocalDateTime fechaTransicionUtc;
}

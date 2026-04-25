package com.logistics.packages.infrastructure.adapter.persistence.eventoprocesado;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para persistir eventos procesados.
 * MOD1-UC-007: FR-008 - Garantiza idempotencia en el procesamiento de eventos.
 */
@Entity
@Table(name = "eventos_procesados")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoProcesadoEntity {
    
    @Id
    @Column(name = "evento_id", nullable = false, unique = true, length = 255)
    private String eventoId;
    
    @Column(name = "paquete_id", nullable = false)
    private UUID paqueteId;
    
    @Column(name = "tipo_evento", nullable = false, length = 100)
    private String tipoEvento;
    
    @Column(name = "fecha_procesamiento_utc", nullable = false)
    private LocalDateTime fechaProcesamientoUtc;
}

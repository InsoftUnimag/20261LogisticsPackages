package com.logistics.packages.domain.model;

import com.logistics.packages.domain.valueobject.EstadoPaquete;
import com.logistics.packages.domain.valueobject.TipoNovedad;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Entidad que representa un registro inmutable en el historial de estados de un paquete.
 * MOD1-UC-006: FR-001, FR-002, FR-003 - Historial cronológico inmutable de transiciones.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class HistorialEstado {
    
    private UUID id;
    
    /**
     * ID del paquete al que pertenece este registro
     */
    private UUID paqueteId;
    
    /**
     * Estado anterior del paquete antes de la transición
     */
    private EstadoPaquete estadoAnterior;
    
    /**
     * Estado nuevo del paquete después de la transición
     */
    private EstadoPaquete estadoNuevo;
    
    /**
     * Observaciones o notas adicionales sobre la transición
     */
    private String observaciones;
    
    /**
     * ID del usuario responsable de la transición (Almacenista)
     */
    private UUID usuarioId;
    
    /**
     * URL de la evidencia multimedia (obligatoria para tipo DAÑADO)
     */
    private String urlEvidencia;

    /**
     * Tipo de novedad (DAÑADO o EXTRAVIADO) cuando aplica
     */
    private TipoNovedad tipoNovedad;
    
    /**
     * FR-001: Timestamp UTC del momento en que se registró la transición
     */
    @Builder.Default
    private LocalDateTime fechaTransicionUtc = LocalDateTime.now(ZoneOffset.UTC);
    
    /**
     * FE-4: Estado de la novedad (PENDIENTE, NOTIFICADO, CERRADO)
     * Persiste el estado del flujo de control de novedades en el frontend
     */
    @Builder.Default
    private String estadoNovedad = "PENDIENTE";
    
    /**
     * Constructor de dominio para crear un nuevo registro de historial.
     * El ID y el timestamp se generan automáticamente.
     * 
     * @param paqueteId ID del paquete
     * @param estadoAnterior Estado previo del paquete
     * @param estadoNuevo Estado nuevo del paquete
     * @param observaciones Notas sobre la transición
     * @param usuarioId ID del usuario responsable
     * @param urlEvidencia URL de la evidencia (si aplica)
     */
    public HistorialEstado(UUID paqueteId, EstadoPaquete estadoAnterior, EstadoPaquete estadoNuevo, 
                          String observaciones, UUID usuarioId, String urlEvidencia) {
        this(paqueteId, estadoAnterior, estadoNuevo, observaciones, usuarioId, urlEvidencia, null);
    }

    public HistorialEstado(UUID paqueteId, EstadoPaquete estadoAnterior, EstadoPaquete estadoNuevo, 
                          String observaciones, UUID usuarioId, String urlEvidencia, TipoNovedad tipoNovedad) {
        this.id = UUID.randomUUID();
        this.paqueteId = paqueteId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.observaciones = observaciones;
        this.usuarioId = usuarioId;
        this.urlEvidencia = urlEvidencia;
        this.tipoNovedad = tipoNovedad;
        this.fechaTransicionUtc = LocalDateTime.now(ZoneOffset.UTC);
        this.estadoNovedad = "PENDIENTE";
    }
}

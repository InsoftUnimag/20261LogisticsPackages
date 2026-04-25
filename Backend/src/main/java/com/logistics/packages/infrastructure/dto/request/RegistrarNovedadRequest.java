package com.logistics.packages.infrastructure.dto.request;

import com.logistics.packages.domain.valueobject.TipoNovedad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO para la petición REST de registrar novedad.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 */
public class RegistrarNovedadRequest {
    
    @NotNull(message = "El ID del paquete es obligatorio")
    private UUID paqueteId;
    
    @NotNull(message = "El tipo de novedad es obligatorio")
    private TipoNovedad tipoNovedad;
    
    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;
    
    @NotBlank(message = "El usuario responsable es obligatorio")
    private String usuarioResponsable;
    
    // Constructor vacío requerido por Jackson
    public RegistrarNovedadRequest() {
    }
    
    public RegistrarNovedadRequest(UUID paqueteId, TipoNovedad tipoNovedad, 
                                  String descripcion, String usuarioResponsable) {
        this.paqueteId = paqueteId;
        this.tipoNovedad = tipoNovedad;
        this.descripcion = descripcion;
        this.usuarioResponsable = usuarioResponsable;
    }
    
    public UUID getPaqueteId() {
        return paqueteId;
    }
    
    public void setPaqueteId(UUID paqueteId) {
        this.paqueteId = paqueteId;
    }
    
    public TipoNovedad getTipoNovedad() {
        return tipoNovedad;
    }
    
    public void setTipoNovedad(TipoNovedad tipoNovedad) {
        this.tipoNovedad = tipoNovedad;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }
    
    public void setUsuarioResponsable(String usuarioResponsable) {
        this.usuarioResponsable = usuarioResponsable;
    }
}

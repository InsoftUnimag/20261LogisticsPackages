package com.logistics.packages.application.command;

import com.logistics.packages.domain.valueobject.TipoNovedad;

import java.util.List;
import java.util.UUID;

/**
 * Comando para registrar una novedad en bodega.
 * Representa la intención del usuario de registrar una novedad.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 */
public class NovedadCommand {
    
    private final UUID paqueteId;
    private final TipoNovedad tipoNovedad;
    private final String descripcion;
    private final String usuarioResponsable;
    private final List<ArchivoEvidenciaCommand> archivosEvidencia;
    
    public NovedadCommand(UUID paqueteId, TipoNovedad tipoNovedad, String descripcion,
                         String usuarioResponsable, List<ArchivoEvidenciaCommand> archivosEvidencia) {
        this.paqueteId = paqueteId;
        this.tipoNovedad = tipoNovedad;
        this.descripcion = descripcion;
        this.usuarioResponsable = usuarioResponsable;
        this.archivosEvidencia = archivosEvidencia;
    }
    
    public UUID getPaqueteId() {
        return paqueteId;
    }
    
    public TipoNovedad getTipoNovedad() {
        return tipoNovedad;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }
    
    public List<ArchivoEvidenciaCommand> getArchivosEvidencia() {
        return archivosEvidencia;
    }
    
    /**
     * Representa un archivo de evidencia en el comando.
     */
    public static class ArchivoEvidenciaCommand {
        private final String nombre;
        private final String contentType;
        private final long tamanio;
        private final byte[] contenido;
        
        public ArchivoEvidenciaCommand(String nombre, String contentType, long tamanio, byte[] contenido) {
            this.nombre = nombre;
            this.contentType = contentType;
            this.tamanio = tamanio;
            this.contenido = contenido;
        }
        
        public String getNombre() {
            return nombre;
        }
        
        public String getContentType() {
            return contentType;
        }
        
        public long getTamanio() {
            return tamanio;
        }
        
        public byte[] getContenido() {
            return contenido;
        }
    }
}

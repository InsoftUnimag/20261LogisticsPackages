package com.logistics.packages.domain.valueobject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Value Object que representa una evidencia multimedia (foto o video).
 * Inmutable por diseño - todos los campos son final.
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 */
public final class Evidencia {
    
    private final String nombreArchivo;
    private final String urlAlmacenamiento;
    private final TipoArchivo tipoArchivo;
    private final long tamanioBytes;
    private final LocalDateTime fechaCaptura;
    
    /**
     * Constructor para crear una nueva evidencia.
     * 
     * @param nombreArchivo Nombre original del archivo
     * @param urlAlmacenamiento URL donde se almacenó el archivo en S3
     * @param tipoArchivo Tipo de archivo (FOTO o VIDEO)
     * @param tamanioBytes Tamaño del archivo en bytes
     * @param fechaCaptura Fecha y hora de captura del archivo
     * @throws IllegalArgumentException si algún parámetro es inválido
     */
    public Evidencia(String nombreArchivo, String urlAlmacenamiento, 
                     TipoArchivo tipoArchivo, long tamanioBytes, 
                     LocalDateTime fechaCaptura) {
        
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
        }
        
        if (urlAlmacenamiento == null || urlAlmacenamiento.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL de almacenamiento no puede estar vacía");
        }
        
        if (tipoArchivo == null) {
            throw new IllegalArgumentException("El tipo de archivo no puede ser nulo");
        }
        
        if (tamanioBytes <= 0) {
            throw new IllegalArgumentException("El tamaño del archivo debe ser positivo");
        }
        
        if (fechaCaptura == null) {
            throw new IllegalArgumentException("La fecha de captura no puede ser nula");
        }
        
        this.nombreArchivo = nombreArchivo;
        this.urlAlmacenamiento = urlAlmacenamiento;
        this.tipoArchivo = tipoArchivo;
        this.tamanioBytes = tamanioBytes;
        this.fechaCaptura = fechaCaptura;
    }
    
    /**
     * Constructor para crear evidencia con fecha de captura actual.
     */
    public Evidencia(String nombreArchivo, String urlAlmacenamiento, 
                     TipoArchivo tipoArchivo, long tamanioBytes) {
        this(nombreArchivo, urlAlmacenamiento, tipoArchivo, tamanioBytes, 
             LocalDateTime.now(ZoneOffset.UTC));
    }
    
    public String getNombreArchivo() {
        return nombreArchivo;
    }
    
    public String getUrlAlmacenamiento() {
        return urlAlmacenamiento;
    }
    
    public TipoArchivo getTipoArchivo() {
        return tipoArchivo;
    }
    
    public long getTamanioBytes() {
        return tamanioBytes;
    }
    
    public LocalDateTime getFechaCaptura() {
        return fechaCaptura;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evidencia evidencia = (Evidencia) o;
        return tamanioBytes == evidencia.tamanioBytes &&
               Objects.equals(nombreArchivo, evidencia.nombreArchivo) &&
               Objects.equals(urlAlmacenamiento, evidencia.urlAlmacenamiento) &&
               tipoArchivo == evidencia.tipoArchivo &&
               Objects.equals(fechaCaptura, evidencia.fechaCaptura);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nombreArchivo, urlAlmacenamiento, tipoArchivo, tamanioBytes, fechaCaptura);
    }
    
    @Override
    public String toString() {
        return "Evidencia{" +
               "nombreArchivo='" + nombreArchivo + '\'' +
               ", tipoArchivo=" + tipoArchivo +
               ", tamanioBytes=" + tamanioBytes +
               ", fechaCaptura=" + fechaCaptura +
               '}';
    }
}

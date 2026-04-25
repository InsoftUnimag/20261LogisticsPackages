package com.logistics.packages.domain.service;

import java.util.UUID;

/**
 * Puerto (interface) para el servicio de almacenamiento de evidencias multimedia.
 * Define el contrato para almacenar archivos en S3/MinIO sin acoplar el dominio.
 * Arquitectura Hexagonal: Puerto de Salida (Output Port)
 * MOD1-IP-006: Actualizar Estado de Paquete por Novedad
 */
public interface EvidenciaStorageService {
    
    /**
     * Almacena un archivo de evidencia en el storage (S3/MinIO).
     * 
     * @param archivo Datos del archivo a almacenar
     * @param paqueteId ID del paquete al que pertenece la evidencia
     * @return URL pública del archivo almacenado
     * @throws StorageException si ocurre un error al almacenar
     */
    String almacenar(ArchivoEvidencia archivo, UUID paqueteId);
    
    /**
     * Elimina un archivo de evidencia del storage.
     * 
     * @param url URL del archivo a eliminar
     * @throws StorageException si ocurre un error al eliminar
     */
    void eliminar(String url);
    
    /**
     * Verifica si un archivo existe en el storage.
     * 
     * @param url URL del archivo
     * @return true si el archivo existe
     */
    boolean existe(String url);
    
    /**
     * Representa los datos de un archivo de evidencia a almacenar.
     */
    interface ArchivoEvidencia {
        String getNombre();
        String getContentType();
        long getTamanio();
        byte[] getContenido();
    }
}

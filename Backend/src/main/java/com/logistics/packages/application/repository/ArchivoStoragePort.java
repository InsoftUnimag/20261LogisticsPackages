package com.logistics.packages.application.repository;

import org.springframework.web.multipart.MultipartFile;

/**
 * Puerto de salida para almacenar archivos multimedia en un sistema de almacenamiento externo (S3).
 * MOD1-UC-006: FR-004 - Gestionar evidencia fotográfica obligatoria.
 */
public interface ArchivoStoragePort {
    
    /**
     * Guarda un archivo en el almacenamiento externo y retorna su URL de acceso.
     * 
     * @param carpeta Carpeta lógica donde se almacenará (ej: "novedades")
     * @param identificador Identificador único para organizar el archivo (ej: UUID del paquete)
     * @param archivo Archivo a guardar
     * @return URL pública o firmada para acceder al archivo
     * @throws RuntimeException si falla el almacenamiento
     */
    String guardar(String carpeta, String identificador, MultipartFile archivo);
}

package com.logistics.packages.infrastructure.adapter.storage;

import com.logistics.packages.application.repository.ArchivoStoragePort;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Adaptador para almacenar archivos en AWS S3.
 * MOD1-UC-006: FR-004 - Implementación del puerto ArchivoStoragePort.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class S3ArchivoStorageAdapter implements ArchivoStoragePort {
    
    private final S3Template s3Template;
    
    @Value("${aws.s3.bucket-name:logistics-packages}")
    private String bucketName;

    @Override
    public String guardar(String carpeta, String identificador, MultipartFile archivo) {
        try {
            // Generar un nombre único para el archivo
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String extension = obtenerExtension(archivo.getOriginalFilename());
            String nombreArchivo = String.format("%s/%s/%s-%s%s", 
                carpeta, identificador, timestamp, identificador, extension);
            
            // Subir el archivo a S3
            s3Template.upload(bucketName, nombreArchivo, archivo.getInputStream());
            
            // Construir y retornar la URL del archivo
            String url = String.format("https://%s.s3.amazonaws.com/%s", bucketName, nombreArchivo);
            
            log.info("Archivo guardado exitosamente en S3: {}", url);
            return url;
            
        } catch (IOException e) {
            log.error("Error al guardar archivo en S3 para el identificador {}: {}", identificador, e.getMessage());
            throw new RuntimeException("Error al almacenar el archivo en S3", e);
        }
    }
    
    /**
     * Extrae la extensión del archivo del nombre original.
     * 
     * @param nombreArchivo Nombre original del archivo
     * @return Extensión del archivo con el punto incluido (ej: ".jpg")
     */
    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return "";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
    }
}

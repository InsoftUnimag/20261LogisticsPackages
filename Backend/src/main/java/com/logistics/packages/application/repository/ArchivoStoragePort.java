package com.logistics.packages.application.repository;

import org.springframework.web.multipart.MultipartFile;

public interface ArchivoStoragePort {

    String guardar(String carpeta, String identificador, MultipartFile archivo);

    void eliminar(String carpeta, String identificador);

    String obtenerUrlFirmada(String carpeta, String identificador, int duracionMinutos);

    byte[] obtenerBytes(String carpeta, String identificador);
}

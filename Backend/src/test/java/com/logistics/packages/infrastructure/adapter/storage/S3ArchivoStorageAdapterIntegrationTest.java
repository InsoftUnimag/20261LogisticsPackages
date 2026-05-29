package com.logistics.packages.infrastructure.adapter.storage;

import com.logistics.packages.application.repository.ArchivoStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfigurationForS3IT.class)
@Tag("integration")
@DisplayName("S3ArchivoStorageAdapter Integration Test")
class S3ArchivoStorageAdapterIntegrationTest {

    @Autowired
    private ArchivoStoragePort archivoStoragePort;

    @Autowired
    private S3Client s3Client;

    private final String carpeta = "test-evidencias";
    private final String identificador = "test-uuid-1234";

    @BeforeEach
    void setUp() {
        String bucketName = "logistics-packages-test";
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        }
    }

    @Test
    @DisplayName("Debe guardar un archivo en S3 y retornar URL")
    void testGuardar() {
        MultipartFile archivo = new MockMultipartFile(
                "evidencia", "foto.jpg", "image/jpeg", "contenido-de-prueba".getBytes()
        );

        String url = archivoStoragePort.guardar(carpeta, identificador, archivo);

        assertNotNull(url);
        assertTrue(url.contains("logistics-packages-test"));
        assertTrue(url.contains(carpeta));
        assertTrue(url.contains(identificador));
    }

    @Test
    @DisplayName("Debe obtener bytes de un archivo previamente guardado")
    void testObtenerBytes() {
        MultipartFile archivo = new MockMultipartFile(
                "evidencia", "documento.pdf", "application/pdf", "datos-binarios".getBytes()
        );

        archivoStoragePort.guardar(carpeta, identificador + "-bytes", archivo);
        byte[] bytes = archivoStoragePort.obtenerBytes(carpeta, identificador + "-bytes");

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        assertEquals("datos-binarios", new String(bytes));
    }

    @Test
    @DisplayName("Debe generar URL firmada para un archivo existente")
    void testObtenerUrlFirmada() {
        MultipartFile archivo = new MockMultipartFile(
                "evidencia", "reporte.png", "image/png", "imagen-firmada".getBytes()
        );

        archivoStoragePort.guardar(carpeta, identificador + "-firmado", archivo);
        String urlFirmada = archivoStoragePort.obtenerUrlFirmada(carpeta, identificador + "-firmado", 5);

        assertNotNull(urlFirmada);
        assertTrue(urlFirmada.contains("X-Amz-Signature"));
    }

    @Test
    @DisplayName("Debe eliminar un archivo de S3")
    void testEliminar() {
        MultipartFile archivo = new MockMultipartFile(
                "evidencia", "temp.jpg", "image/jpeg", "para-eliminar".getBytes()
        );

        archivoStoragePort.guardar(carpeta, identificador + "-eliminar", archivo);

        archivoStoragePort.eliminar(carpeta, identificador + "-eliminar");

        assertThrows(RuntimeException.class,
                () -> archivoStoragePort.obtenerBytes(carpeta, identificador + "-eliminar"));
    }

    @Test
    @DisplayName("Debe lanzar excepción al obtener bytes de un archivo inexistente")
    void testObtenerBytesArchivoInexistente() {
        assertThrows(RuntimeException.class,
                () -> archivoStoragePort.obtenerBytes(carpeta, "uuid-inexistente"));
    }

    @Test
    @DisplayName("Debe lanzar excepción al generar URL firmada de archivo inexistente")
    void testObtenerUrlFirmadaArchivoInexistente() {
        assertThrows(RuntimeException.class,
                () -> archivoStoragePort.obtenerUrlFirmada(carpeta, "uuid-inexistente", 5));
    }
}

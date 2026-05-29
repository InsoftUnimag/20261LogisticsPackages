package com.logistics.packages.infrastructure.adapter.storage;

import com.logistics.packages.application.repository.ArchivoStoragePort;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3ArchivoStorageAdapter implements ArchivoStoragePort {

    private final S3Template s3Template;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name:logistics-packages}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static:us-east-2}")
    private String awsRegion;

    @Value("${spring.cloud.aws.s3.endpoint:}")
    private String s3Endpoint;

    private S3Presigner presigner;

    @Override
    public String guardar(String carpeta, String identificador, MultipartFile archivo) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String extension = obtenerExtension(archivo.getOriginalFilename());
            String nombreArchivo = String.format("%s/%s/%s-%s%s",
                carpeta, identificador, timestamp, identificador, extension);

            s3Template.upload(bucketName, nombreArchivo, archivo.getInputStream());

            String url = String.format("https://%s.s3.amazonaws.com/%s", bucketName, nombreArchivo);

            log.info("Archivo guardado exitosamente en S3: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Error al guardar archivo en S3 para el identificador {}: {}", identificador, e.getMessage());
            throw new RuntimeException("Error al almacenar el archivo en S3", e);
        }
    }

    @Override
    public void eliminar(String carpeta, String identificador) {
        List<String> keys = listarClaves(carpeta, identificador);
        if (keys.isEmpty()) {
            log.warn("No se encontraron archivos para eliminar en {}/{}", carpeta, identificador);
            return;
        }
        for (String key : keys) {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            log.info("Archivo eliminado de S3: {}/{}", bucketName, key);
        }
    }

    @Override
    public String obtenerUrlFirmada(String carpeta, String identificador, int duracionMinutos) {
        List<String> keys = listarClaves(carpeta, identificador);
        if (keys.isEmpty()) {
            throw new RuntimeException("No se encontraron archivos en S3 para " + carpeta + "/" + identificador);
        }
        String key = keys.get(0);

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(duracionMinutos))
                .getObjectRequest(req -> req.bucket(bucketName).key(key))
                .build();

        URL presignedUrl = obtenerPresigner().presignGetObject(presignRequest).url();
        log.info("URL firmada generada para {}/{}", carpeta, identificador);
        return presignedUrl.toString();
    }

    @Override
    public byte[] obtenerBytes(String carpeta, String identificador) {
        List<String> keys = listarClaves(carpeta, identificador);
        if (keys.isEmpty()) {
            throw new RuntimeException("No se encontraron archivos en S3 para " + carpeta + "/" + identificador);
        }
        String key = keys.get(0);

        byte[] bytes = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build())
                .asByteArray();

        log.info("Bytes obtenidos de S3 para {}/{}: {} bytes", carpeta, identificador, bytes.length);
        return bytes;
    }

    private List<String> listarClaves(String carpeta, String identificador) {
        String prefix = String.format("%s/%s/", carpeta, identificador);
        ListObjectsV2Response response = s3Client.listObjectsV2(
                ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .build());
        return response.contents().stream()
                .map(S3Object::key)
                .toList();
    }

    private S3Presigner obtenerPresigner() {
        if (presigner == null) {
            var builder = S3Presigner.builder()
                    .region(Region.of(awsRegion));
            if (s3Endpoint != null && !s3Endpoint.isBlank()) {
                builder.endpointOverride(URI.create(s3Endpoint));
            }
            presigner = builder.build();
        }
        return presigner;
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return "";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
    }
}

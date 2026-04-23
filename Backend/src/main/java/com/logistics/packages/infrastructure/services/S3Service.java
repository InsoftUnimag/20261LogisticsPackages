package com.logistics.packages.infrastructure.services;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de ejemplo para interactuar con AWS S3.
 * <p>
 * Este servicio utiliza el S3Client configurado por Spring Cloud AWS para
 * realizar operaciones básicas, como listar buckets, y así verificar la
 * conectividad con el endpoint de S3 (en este caso, LocalStack).
 */
@Service
@AllArgsConstructor
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;

    /**
     * Lista todos los buckets de S3 disponibles.
     * <p>
     * Este método es útil para una verificación rápida de la conexión.
     * Si la configuración (endpoint, región, credenciales) es correcta,
     * devolverá una lista de los buckets existentes en el servicio S3.
     */
    public void listBuckets() {
        logger.info("Solicitando lista de buckets de S3...");
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            logger.info("Buckets encontrados ({}):", response.buckets().size());
            response.buckets().forEach(bucket -> logger.info("- {}", bucket.name()));
        } catch (Exception e) {
            logger.error("Error al intentar listar los buckets de S3. " +
                         "Verifica la configuración y que LocalStack esté corriendo.", e);
        }
    }
}

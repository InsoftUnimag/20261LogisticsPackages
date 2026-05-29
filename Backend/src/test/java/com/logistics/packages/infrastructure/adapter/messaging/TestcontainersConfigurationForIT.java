package com.logistics.packages.infrastructure.adapter.messaging;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Configuración de TestContainers para tests de integración SQS.
 * Levanta LocalStack (SQS + S3) y PostgreSQL.
 * Los clients S3/SQS/S3 son auto-configurados por Spring Cloud AWS
 * usando las propiedades dinámicas apuntando a LocalStack.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfigurationForIT {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:10-alpine"));
    }

    private static LocalStackContainer localstack;

    @Bean
    static LocalStackContainer localstackContainer() {
        if (localstack == null) {
            localstack = new LocalStackContainer(
                    DockerImageName.parse("localstack/localstack:3.4.0")
            ).withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.S3);
            localstack.start();
        }
        return localstack;
    }

    @DynamicPropertySource
    static void initializeLocalStackProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.endpoint", () -> localstackContainer().getEndpoint().toString());
        registry.add("spring.cloud.aws.credentials.access-key", localstackContainer()::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", localstackContainer()::getSecretKey);
        registry.add("spring.cloud.aws.region.static", localstackContainer()::getRegion);
        registry.add("spring.cloud.aws.s3.endpoint", () -> localstackContainer().getEndpoint().toString());
        registry.add("spring.cloud.aws.s3.path-style-access-enabled", () -> "true");
        registry.add("aws.s3.bucket-name", () -> "logistics-packages-test");
        registry.add("spring.cloud.aws.sqs.ruta-request-queue", () -> "test-solicitar-ruta-queue");
        registry.add("spring.cloud.aws.sqs.ruta-response-queue", () -> "test-respuestas-ruta-queue");
    }
}

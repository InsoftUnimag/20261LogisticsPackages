package com.example.demo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:10-alpine"));
    }

    // 1. Instancia estática (se crea una sola vez)
    static final LocalStackContainer localstack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.4.0")
    ).withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.S3);

    @Bean
    LocalStackContainer localstackContainer() {
        // 2. El bean solo expone la instancia estática
        return localstack;
    }

    @DynamicPropertySource
    static void initializeLocalStackProperties(DynamicPropertyRegistry registry) {
        // 3. Usamos la instancia estática directamente
        registry.add("spring.cloud.aws.endpoint", localstack::getEndpoint);
        registry.add("spring.cloud.aws.credentials.access-key", localstack::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", localstack::getSecretKey);
        registry.add("spring.cloud.aws.region.static", localstack::getRegion);
    }
}

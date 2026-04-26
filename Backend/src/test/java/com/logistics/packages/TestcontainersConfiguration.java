package com.logistics.packages;

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
    }
}
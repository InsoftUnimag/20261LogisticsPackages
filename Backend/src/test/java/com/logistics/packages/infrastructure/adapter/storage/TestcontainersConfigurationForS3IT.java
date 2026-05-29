package com.logistics.packages.infrastructure.adapter.storage;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfigurationForS3IT {

    private static LocalStackContainer localstack;

    @Bean
    @ServiceConnection
    LocalStackContainer localstackContainer() {
        if (localstack == null) {
            localstack = new LocalStackContainer(
                    DockerImageName.parse("localstack/localstack:3.4.0")
            ).withServices(LocalStackContainer.Service.S3);
            localstack.start();
        }
        return localstack;
    }

    @DynamicPropertySource
    static void initializeLocalStackProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.s3.path-style-access-enabled", () -> "true");
        registry.add("aws.s3.bucket-name", () -> "logistics-packages-test");
    }
}

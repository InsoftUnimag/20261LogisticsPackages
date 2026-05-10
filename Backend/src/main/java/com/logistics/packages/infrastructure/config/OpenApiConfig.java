package com.logistics.packages.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI logisticsPackagesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Logistics & Packages API")
                        .version("1.0.0")
                        .description("Contrato de interfaz para la integración del repositorio Frontend."));
    }
}

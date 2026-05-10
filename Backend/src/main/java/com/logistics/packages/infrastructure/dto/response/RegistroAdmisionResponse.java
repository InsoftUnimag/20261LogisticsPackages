package com.logistics.packages.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RegistroAdmisionResponse {
    @Schema(description = "ID único del paquete registrado en el sistema")
    private UUID paqueteId;
}

package com.logistics.packages.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    @Schema(description = "Timestamp del error en UTC (ISO-8601)")
    private Instant timestamp;

    @Schema(description = "Código HTTP de la respuesta")
    private int status;

    @Schema(description = "Código interno del error")
    private String codigo;

    @Schema(description = "Mensaje descriptivo del error para el usuario")
    private String mensaje;

    @Schema(description = "Errores de validación por campo (si aplica)")
    private List<ValidationError> errores;

    public ApiError(int status, String codigo, String mensaje) {
        this(Instant.now(), status, codigo, mensaje, null);
    }

    public ApiError(int status, String codigo, String mensaje, List<ValidationError> errores) {
        this(Instant.now(), status, codigo, mensaje, errores);
    }

    @Getter
    @AllArgsConstructor
    public static class ValidationError {
        @Schema(description = "Nombre del campo con error")
        private String campo;

        @Schema(description = "Mensaje de validación")
        private String mensaje;
    }
}

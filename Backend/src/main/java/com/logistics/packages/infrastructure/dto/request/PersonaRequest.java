package com.logistics.packages.infrastructure.dto.request;

import com.logistics.packages.domain.valueobject.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * FIX Bug 7: DTO dedicado para deserialización HTTP de personas.
 * Separa la capa HTTP del dominio evitando problemas con Jackson
 * al deserializar clases inmutables del dominio (con campos final).
 * 
 * Este DTO tiene campos mutables simples que Jackson puede procesar
 * correctamente, luego se mapea explícitamente a Persona en el controlador.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaRequest {

    @NotNull(message = "El tipo de documento es requerido")
    @Schema(description = "Tipo de documento de identidad")
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "El número de documento es requerido")
    @Schema(description = "Número único de identificación")
    private String numeroDocumento;

    @NotBlank(message = "El nombre completo es requerido")
    @Schema(description = "Nombre completo de la persona")
    private String nombreCompleto;

    @NotBlank(message = "El teléfono es requerido")
    @Schema(description = "Teléfono de contacto")
    private String telefono;

    @Schema(description = "Correo electrónico de contacto (opcional para remitente)")
    private String correoElectronico;
}

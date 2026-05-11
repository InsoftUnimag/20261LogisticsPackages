package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.repository.ConsultarPaqueteIn;
import com.logistics.packages.application.repository.RegistrarAdmisionIn;
import com.logistics.packages.application.usecase.RegistroAdmisionCommand;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.infrastructure.dto.request.RegistroAdmisionRequest;
import com.logistics.packages.infrastructure.dto.response.ConsultaPaqueteResponse;
import com.logistics.packages.infrastructure.dto.response.RegistroAdmisionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Tag(name = "Admisión", description = "Registro y consulta de paquetes")
@RestController
@RequestMapping("/api/paquetes")
@AllArgsConstructor
public class AdmisionController {

    private final RegistrarAdmisionIn registrarAdmisionIn;
    private final ConsultarPaqueteIn consultarPaqueteIn;

    @Operation(summary = "Registrar admisión de paquete", description = "Registra un nuevo paquete en el sistema con datos del remitente, destinatario, tipo de mercancía y método de pago. Retorna el ID asignado al paquete")
    @ApiResponse(responseCode = "200", description = "Paquete registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Error de validación en los datos de entrada")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping("/admision")
    public ResponseEntity<RegistroAdmisionResponse> registrarAdmision(@Valid @RequestBody RegistroAdmisionRequest request) {
        RegistroAdmisionCommand command = RegistroAdmisionCommand.builder()
                .sedeId(request.getSedeId())
                .direccionDestino(request.getDireccionDestino())
                .valorDeclarado(request.getValorDeclarado())
                .metodoPago(request.getMetodoPago())
                .remitente(request.getRemitente())
                .destinatario(request.getDestinatario())
                .tipoMercancia(request.getTipoMercancia())
                .indicadorFormaIrregular(request.getIndicadorFormaIrregular())
                .peso(request.getPeso())
                .largo(request.getLargo())
                .ancho(request.getAncho())
                .alto(request.getAlto())
                .build();

        UUID paqueteId = registrarAdmisionIn.registrarAdmision(command);

        return ResponseEntity.ok(new RegistroAdmisionResponse(paqueteId));
    }

    @Operation(summary = "Consultar paquete por ID", description = "Obtiene la información básica de un paquete (ID de ruta, ID de paquete y estado actual) dado su ID único")
    @ApiResponse(responseCode = "200", description = "Paquete encontrado")
    @ApiResponse(responseCode = "404", description = "Paquete no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/{idPaquete}")
    public ResponseEntity<ConsultaPaqueteResponse> consultarPaquete(@PathVariable UUID idPaquete) {
        Optional<Paquete> paqueteOpt = consultarPaqueteIn.consultarPaquete(idPaquete);

        return paqueteOpt
                .map(paquete -> ResponseEntity.ok(new ConsultaPaqueteResponse(
                        paquete.getRutaId(),
                        paquete.getId(),
                        paquete.getEstado())))
                .orElse(ResponseEntity.notFound().build());
    }
}
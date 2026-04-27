package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.repository.ConsultarPaqueteIn;
import com.logistics.packages.application.repository.RegistrarAdmisionIn;
import com.logistics.packages.application.usecase.RegistroAdmisionCommand;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.infrastructure.dto.request.RegistroAdmisionRequest;
import com.logistics.packages.infrastructure.dto.response.ConsultaPaqueteResponse;
import com.logistics.packages.infrastructure.dto.response.RegistroAdmisionResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/paquetes")
@AllArgsConstructor
public class AdmisionController {

    private final RegistrarAdmisionIn registrarAdmisionIn;
    private final ConsultarPaqueteIn consultarPaqueteIn;

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
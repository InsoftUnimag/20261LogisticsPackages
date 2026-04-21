package com.logistics.packages.infrastructure.controller;

import com.logistics.packages.application.admision.repositories.ConsultarPaqueteIn;
import com.logistics.packages.application.admision.repositories.RegistrarAdmisionIn;
import com.logistics.packages.application.admision.usecase.RegistroAdmisionCommand;
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
@RequestMapping("/")
@AllArgsConstructor
public class AdmisionController {

    private final RegistrarAdmisionIn registrarAdmisionIn;
    private final ConsultarPaqueteIn consultarPaqueteIn;

    @PostMapping("/admision/registrar")
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

    @GetMapping("/route/{idRoute}/package/{idPaquete}")
    public ResponseEntity<ConsultaPaqueteResponse> consultarPaquete(@PathVariable UUID idRoute, @PathVariable UUID idPaquete) {
        Optional<Paquete> paqueteOpt = consultarPaqueteIn.consultarPaquete(idPaquete);

        return paqueteOpt
                .filter(paquete -> idRoute.equals(paquete.getRutaId()))
                .map(paquete -> ResponseEntity.ok(new ConsultaPaqueteResponse(
                        paquete.getRutaId(),
                        paquete.getId(),
                        paquete.getEstado())))
                .orElse(ResponseEntity.notFound().build());
    }
}

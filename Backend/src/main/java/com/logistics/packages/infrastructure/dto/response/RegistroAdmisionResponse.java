package com.logistics.packages.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RegistroAdmisionResponse {
    private UUID paqueteId;
}

package com.logistics.packages.application.ports.in;

import com.logistics.packages.application.usecase.RegistroAdmisionCommand;

import java.util.UUID;

public interface RegistrarAdmisionIn {
    UUID registrarAdmision(RegistroAdmisionCommand command);
}

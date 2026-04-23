package com.logistics.packages.application.admision.repositories;

import com.logistics.packages.application.admision.usecase.RegistroAdmisionCommand;

import java.util.UUID;

public interface RegistrarAdmisionIn {
    UUID registrarAdmision(RegistroAdmisionCommand command);
}

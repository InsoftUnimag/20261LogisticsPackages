package com.logistics.packages.application.repository;

import com.logistics.packages.application.usecase.RegistroAdmisionCommand;

import java.util.UUID;

public interface RegistrarAdmisionIn {
    UUID registrarAdmision(RegistroAdmisionCommand command);
}

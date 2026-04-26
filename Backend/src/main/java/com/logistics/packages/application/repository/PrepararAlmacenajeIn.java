package com.logistics.packages.application.repository;

import com.logistics.packages.application.usecase.PrepararAlmacenajeCommand;

public interface PrepararAlmacenajeIn {
    void prepararAlmacenaje(PrepararAlmacenajeCommand command);
}

package com.logistics.packages.infrastructure.adapter.persistence.usuario;

import com.logistics.packages.domain.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "passwordHash", source = "passwordHash")
    UsuarioEntity toEntity(Usuario domain);

    Usuario toDomain(UsuarioEntity entity);
}

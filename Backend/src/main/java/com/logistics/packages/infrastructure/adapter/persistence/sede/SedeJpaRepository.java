package com.logistics.packages.infrastructure.adapter.persistence.sede;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio Spring Data JPA para SedeDbo.
 * Proporciona acceso a la tabla 'sedes'.
 */
@Repository
public interface SedeJpaRepository extends JpaRepository<SedeDbo, UUID> {
}

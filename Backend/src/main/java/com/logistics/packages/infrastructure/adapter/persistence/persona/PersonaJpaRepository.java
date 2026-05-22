package com.logistics.packages.infrastructure.adapter.persistence.persona;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonaJpaRepository extends JpaRepository<PersonaDbo, UUID> {
    /**
     * Busca una persona por su número de documento
     * @param numeroDocumento Número único de identificación
     * @return Optional con la persona si existe
     */
    Optional<PersonaDbo> findByNumeroDocumento(String numeroDocumento);
}

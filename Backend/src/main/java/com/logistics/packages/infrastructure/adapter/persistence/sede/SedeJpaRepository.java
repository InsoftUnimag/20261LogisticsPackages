package com.logistics.packages.infrastructure.adapter.persistence.sede;

import com.logistics.packages.domain.valueobject.TipoSede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad SedeDbo.
 * Proporciona acceso a la base de datos para operaciones CRUD.
 */
@Repository
public interface SedeJpaRepository extends JpaRepository<SedeDbo, UUID> {
    
    /**
     * Busca todas las sedes de un tipo específico.
     * @param tipo Tipo de sede (PRINCIPAL o AUXILIAR)
     * @return Lista de sedes del tipo especificado
     */
    List<SedeDbo> findByTipo(TipoSede tipo);
}

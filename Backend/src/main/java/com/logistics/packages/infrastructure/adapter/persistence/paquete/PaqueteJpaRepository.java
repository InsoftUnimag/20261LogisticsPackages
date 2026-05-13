package com.logistics.packages.infrastructure.adapter.persistence.paquete;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaqueteJpaRepository extends JpaRepository<PaqueteDbo, UUID>, JpaSpecificationExecutor<PaqueteDbo> {
}

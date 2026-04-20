package com.logistics.packages.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaqueteJpaRepository extends JpaRepository<PaqueteDbo, UUID> {
}

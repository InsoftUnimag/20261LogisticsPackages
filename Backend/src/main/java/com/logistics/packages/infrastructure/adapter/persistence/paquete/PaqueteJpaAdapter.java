package com.logistics.packages.infrastructure.adapter.persistence.paquete;

import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.valueobject.EstadoPaquete;
import com.logistics.packages.infrastructure.adapter.persistence.persona.PersonaDbo;
import com.logistics.packages.infrastructure.adapter.persistence.persona.PersonaJpaRepository;
import com.logistics.packages.infrastructure.adapter.persistence.persona.PersonaMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class PaqueteJpaAdapter implements PaqueteRepository {

    private final PaqueteJpaRepository paqueteJpaRepository;
    private final PaqueteMapper paqueteMapper;
    private final PersonaJpaRepository personaJpaRepository;
    private final PersonaMapper personaMapper;

    @Override
    public Paquete save(Paquete paquete) {
        PaqueteDbo dbo = paqueteMapper.toDbo(paquete);
        
        // FIX Bug 1 y 2: Reutilizar personas existentes por número de documento
        // en lugar de crear nuevas instancias cada vez que se guarda el paquete
        if (paquete.getRemitente() != null && paquete.getRemitente().getNumeroDocumento() != null) {
            Optional<PersonaDbo> remitenteExistente = personaJpaRepository
                    .findByNumeroDocumento(paquete.getRemitente().getNumeroDocumento());
            
            if (remitenteExistente.isPresent()) {
                // Reutilizar la persona existente en lugar de crear una nueva
                dbo.setRemitente(remitenteExistente.get());
            } else {
                // Si no existe, guardar explícitamente la persona nueva para evitar que
                // los campos se persistan como nulos (problema con GenerationType.AUTO y cascade)
                PersonaDbo nuevoRemitente = personaJpaRepository.save(personaMapper.toDbo(paquete.getRemitente()));
                dbo.setRemitente(nuevoRemitente);
            }
        }
        
        if (paquete.getDestinatario() != null && paquete.getDestinatario().getNumeroDocumento() != null) {
            Optional<PersonaDbo> destinatarioExistente = personaJpaRepository
                    .findByNumeroDocumento(paquete.getDestinatario().getNumeroDocumento());
            
            if (destinatarioExistente.isPresent()) {
                // Reutilizar la persona existente en lugar de crear una nueva
                dbo.setDestinatario(destinatarioExistente.get());
            } else {
                // Si no existe, guardar explícitamente la persona nueva para evitar que
                // los campos se persistan como nulos (problema con GenerationType.AUTO y cascade)
                PersonaDbo nuevoDestinatario = personaJpaRepository.save(personaMapper.toDbo(paquete.getDestinatario()));
                dbo.setDestinatario(nuevoDestinatario);
            }
        }
        
        // Guardar el paquete DBO con referencias a personas existentes/nuevas
        paqueteJpaRepository.save(dbo);

        // Retornamos el paquete domain original (que está en memoria y completamente válido)
        // La identidad del paquete nace en el dominio (crearNuevo), JPA no la gestiona
        return paquete;
    }

    @Override
    public Optional<Paquete> findById(UUID id) {
        return paqueteJpaRepository.findById(id).map(paqueteMapper::toDomain);
    }

    @Override
    public Page<Paquete> findAll(EstadoPaquete estado, LocalDateTime fechaDesde, LocalDateTime fechaHasta, Pageable pageable) {
        Specification<PaqueteDbo> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (estado != null) {
                predicate = cb.and(predicate, cb.equal(root.get("estado"), estado));
            }
            if (fechaDesde != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("fechaIngresoUtc"), fechaDesde));
            }
            if (fechaHasta != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("fechaIngresoUtc"), fechaHasta));
            }
            return predicate;
        };
        return paqueteJpaRepository.findAll(spec, pageable).map(paqueteMapper::toDomain);
    }
}

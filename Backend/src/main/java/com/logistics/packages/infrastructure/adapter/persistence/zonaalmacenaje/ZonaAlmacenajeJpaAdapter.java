package com.logistics.packages.infrastructure.adapter.persistence.zonaalmacenaje;

import com.logistics.packages.application.repository.ZonaAlmacenajeRepository;
import com.logistics.packages.domain.model.ZonaAlmacenaje;
import com.logistics.packages.domain.valueobject.CategoriaZona;
import com.logistics.packages.domain.valueobject.TipoMercancia;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class ZonaAlmacenajeJpaAdapter implements ZonaAlmacenajeRepository {

    private final ZonaAlmacenajeJpaRepository zonaAlmacenajeJpaRepository;
    private final ZonaAlmacenajeMapper zonaAlmacenajeMapper;

    @Override
    public Optional<ZonaAlmacenaje> findById(UUID id) {
        return zonaAlmacenajeJpaRepository.findById(id).map(zonaAlmacenajeMapper::toDomain);
    }

    @Override
    public ZonaAlmacenaje save(ZonaAlmacenaje zona) {
        ZonaAlmacenajeDbo dbo = zonaAlmacenajeMapper.toDbo(zona);
        ZonaAlmacenajeDbo savedDbo = zonaAlmacenajeJpaRepository.save(dbo);
        return zonaAlmacenajeMapper.toDomain(savedDbo);
    }

    @Override
    public List<ZonaAlmacenaje> findCompatibleZonesWithCapacity(TipoMercancia tipoMercancia, UUID sedeId) {
        return zonaAlmacenajeJpaRepository.findByIdSede(sedeId).stream()
                .filter(z -> z.getCategoria().esCompatible(tipoMercancia) && z.getContadorPaquetes() < z.getCapacidadMaxPaquetes())
                .map(zonaAlmacenajeMapper::toDomain)
                .toList();
    }

    @Override
    public List<ZonaAlmacenaje> findBySedeId(UUID sedeId) {
        return zonaAlmacenajeJpaRepository.findByIdSede(sedeId).stream()
                .map(zonaAlmacenajeMapper::toDomain)
                .toList();
    }

    @Override
    public List<ZonaAlmacenaje> findByCategoriaAndSedeId(CategoriaZona categoria, UUID sedeId) {
        return zonaAlmacenajeJpaRepository.findByIdSede(sedeId).stream()
                .filter(z -> z.getCategoria().equals(categoria))
                .map(zonaAlmacenajeMapper::toDomain)
                .toList();
    }
}

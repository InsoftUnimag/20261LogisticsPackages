package com.logistics.packages.application.repository;

import com.logistics.packages.domain.model.ZonaAlmacenaje;
import com.logistics.packages.domain.valueobject.CategoriaZona;
import com.logistics.packages.domain.valueobject.TipoMercancia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ZonaAlmacenajeRepository {

    ZonaAlmacenaje save(ZonaAlmacenaje zonaAlmacenaje);

    Optional<ZonaAlmacenaje> findById(UUID id);

    List<ZonaAlmacenaje> findCompatibleZonesWithCapacity(TipoMercancia tipoMercancia, UUID sedeId);

    List<ZonaAlmacenaje> findBySedeId(UUID sedeId);

    List<ZonaAlmacenaje> findByCategoriaAndSedeId(CategoriaZona categoria, UUID sedeId);
}

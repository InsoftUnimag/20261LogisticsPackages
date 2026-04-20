package com.logistics.packages.application.ports.out;

import com.logistics.packages.domain.model.ZonaAlmacenaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ZonaAlmacenajeRepository extends JpaRepository<ZonaAlmacenaje, UUID> {

    List<ZonaAlmacenaje> findByCategoriaAndEstado(ZonaAlmacenaje.CategoriaZona categoria, ZonaAlmacenaje.EstadoZona estado);

    List<ZonaAlmacenaje> findByIdSedeAndCategoriaAndEstado(UUID idSede, ZonaAlmacenaje.CategoriaZona categoria, ZonaAlmacenaje.EstadoZona estado);
}

package com.logistics.packages.application.usecase;

import com.logistics.packages.application.repository.PaqueteRepository;
import com.logistics.packages.application.repository.PrepararAlmacenajeIn;
import com.logistics.packages.application.repository.ZonaAlmacenajeRepository;
import com.logistics.packages.domain.exception.PaqueteNotFoundException;
import com.logistics.packages.domain.exception.ZonaAlmacenajeNotFoundException;
import com.logistics.packages.domain.exception.ZonaIncompatibleException;
import com.logistics.packages.domain.exception.ZonaSaturadaException;
import com.logistics.packages.domain.model.Paquete;
import com.logistics.packages.domain.model.ZonaAlmacenaje;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class PrepararAlmacenajeUseCase implements PrepararAlmacenajeIn {

    private final PaqueteRepository paqueteRepository;
    private final ZonaAlmacenajeRepository zonaAlmacenajeRepository;

    @Override
    public void prepararAlmacenaje(PrepararAlmacenajeCommand command) {
        Paquete paquete = paqueteRepository.findById(command.paqueteId())
                .orElseThrow(() -> new PaqueteNotFoundException(command.paqueteId()));

        ZonaAlmacenaje zona = zonaAlmacenajeRepository.findById(command.zonaAlmacenamientoId())
                .orElseThrow(() -> new ZonaAlmacenajeNotFoundException(command.zonaAlmacenamientoId()));

        if (!zona.puedeAlbergar(paquete)) {
            throw new ZonaIncompatibleException(paquete.getId(), paquete.getTipoMercancia(), zona.getId(), zona.getCategoria());
        }

        if (!zona.tieneCapacidadPara(paquete)) {
            throw new ZonaSaturadaException(zona.getId(), zona.getNombre(), "capacidad", zona.getZonaContingenciaId());
        }

        paquete.asignarZonaAlmacenamiento(zona.getId());
        zona.agregarPaquete(paquete);

        paqueteRepository.save(paquete);
        zonaAlmacenajeRepository.save(zona);
    }
}

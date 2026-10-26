package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdBloqueo
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

/** Adaptador en memoria del puerto (Guía 06), bean de Spring (Guía 09) */
@Repository
public class BloqueoRepositoryInMemory implements BloqueoRepository {

    private final HashMap<IdBloqueo, Bloqueo> bloqueos = new HashMap<>();

    public void guardar(Bloqueo bloqueo) {
        bloqueos.put(bloqueo.getId(), bloqueo);
    }

    @Override
    public List<Bloqueo> buscarVigentesByApartamento(IdentificacionApartamento apartamento) {
        return List.of();
    }
}

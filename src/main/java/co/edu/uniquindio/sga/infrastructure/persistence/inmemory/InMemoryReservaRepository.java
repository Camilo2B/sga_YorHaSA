package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación en memoria usando HashMap exigida por la rúbrica para la Entrega 1.
 */
public class InMemoryReservaRepository implements ReservaRepository {

    private final Map<UUID, Reserva> tablaReservas = new HashMap<>();

    @Override
    public void guardar(Reserva reserva) {
        tablaReservas.put(reserva.getId(), reserva);
    }

    @Override
    public Optional<Reserva> buscarPorId(UUID id) {
        return Optional.ofNullable(tablaReservas.get(id));
    }

    @Override
    public List<Reserva> buscarPorApartamento(String apartamentoId) {
        return tablaReservas.values().stream()
                .filter(r -> r.getApartamentoId().toString().equals(apartamentoId)
                        || r.getApartamentoId().equals(UUID.nameUUIDFromBytes(apartamentoId.getBytes())))
                .toList();
    }

    @Override
    public List<Reserva> obtenerTodas() {
        return new ArrayList<>(tablaReservas.values());
    }
}
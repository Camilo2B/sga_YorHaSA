package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Adaptador en memoria: solo para pruebas unitarias del dominio. La aplicación usa ReservaJpaRepository. */
public class ReservaRepositoryInMemory implements ReservaRepository {

    private final Map<UUID, Reserva> tablaReservas = new HashMap<>();

    @Override
    public List<Reserva> buscarActivasPorApartamento(String apartamentoId, Estancia estancia) {
        return tablaReservas.values().stream()
                .filter(r -> r.getApartamentoId().equals(apartamentoId))
                .filter(r -> r.getEstado().esActiva())
                .filter(r -> r.getEstancia().seSolapaCon(estancia))
                .toList();
    }

    @Override
    public Optional<Reserva> obtenerPorCodigo(CodigoReserva codigo) {
        return tablaReservas.values().stream().filter(r -> r.getCodigo().equals(codigo)).findFirst();
    }

    @Override
    public List<Reserva> buscarPorEstado(EstadoReserva estado) {
        return tablaReservas.values().stream()
                .filter(r -> r.getEstado() == estado)
                .toList();
    }

    @Override
    public void guardar(Reserva reserva) {
        tablaReservas.put(reserva.getId(), reserva);
    }
}

package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;

import java.util.List;
import java.util.Objects;

/**
 * 7.3: administración del inventario de apartamentos. Resuelve las reglas que ningún Apartamento puede evaluar solo
 * porque exigen mirar fuera del agregado:
 * <ul>
 *   <li>L-04: la identificación de cada apartamento es única y estable (consulta el repositorio de apartamentos).</li>
 *   <li>Un apartamento solo se retira de la venta si no tiene reservas activas ni futuras (consulta las reservas).</li>
 *   <li>RN-02: la capacidad no puede bajar de los cupos que ya ocupa una reserva activa.</li>
 * </ul>
 */
public class RegistroApartamentoService {

    private final ApartamentoRepository apartamentoRepository;
    private final ReservaRepository reservaRepository;

    public RegistroApartamentoService(ApartamentoRepository apartamentoRepository, ReservaRepository reservaRepository) {
        this.apartamentoRepository = Objects.requireNonNull(apartamentoRepository);
        this.reservaRepository = Objects.requireNonNull(reservaRepository);
    }

    public void registrar(Apartamento apartamento) {
        Objects.requireNonNull(apartamento, "El apartamento no puede ser nulo");
        if (apartamentoRepository.buscarPorId(apartamento.identificacion()).isPresent()) {
            throw new ReglaDominioException(
                    "Ya existe un apartamento con la identificación " + apartamento.identificacion() + " (L-04)");
        }
        apartamentoRepository.guardar(apartamento);
    }

    /** 7.3 / 12.4: eliminación lógica. Las reservas históricas siguen referenciando al apartamento. Quien invoca lo guarda. */
    public void retirar(Apartamento apartamento) {
        Objects.requireNonNull(apartamento, "El apartamento no puede ser nulo");
        List<Reserva> activas = reservaRepository.buscarActivasPorApartamento(apartamento.identificacion());
        if (!activas.isEmpty()) {
            throw new ReglaDominioException("7.3: no se puede retirar el apartamento " + apartamento.identificacion()
                    + " porque tiene " + activas.size() + " reserva(s) activa(s) o futura(s)");
        }
        apartamento.desactivar();
    }

    /** RN-02: la nueva capacidad debe alcanzar para cada reserva activa del apartamento. Quien invoca lo guarda. */
    public void cambiarCapacidad(Apartamento apartamento, int nuevaCapacidad) {
        Objects.requireNonNull(apartamento, "El apartamento no puede ser nulo");
        reservaRepository.buscarActivasPorApartamento(apartamento.identificacion()).stream()
                .filter(reserva -> reserva.cuposNecesarios() > nuevaCapacidad)
                .findFirst()
                .ifPresent(reserva -> {
                    throw new ReglaDominioException("RN-02: la reserva " + reserva.getCodigo().valor() + " ocupa "
                            + reserva.cuposNecesarios() + " cupos; la capacidad del apartamento " + apartamento.identificacion()
                            + " no puede bajar a " + nuevaCapacidad);
                });
        apartamento.cambiarCapacidad(nuevaCapacidad);
    }
}

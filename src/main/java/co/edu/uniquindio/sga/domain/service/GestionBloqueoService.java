package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

import java.util.List;
import java.util.Objects;

/**
 * Servicio de Dominio para validar y registrar bloqueos de mantenimiento u operativos en los apartamentos.
 * Garantiza la regla 7.3: no se pueden crear bloqueos sobre fechas con reservas activas.
 */
public class GestionBloqueoService {

    private final ApartamentoRepository apartamentoRepository;
    private final ReservaRepository reservaRepository;

    public GestionBloqueoService(ApartamentoRepository apartamentoRepository, ReservaRepository reservaRepository) {
        this.apartamentoRepository = Objects.requireNonNull(apartamentoRepository, "El repositorio de apartamentos no puede ser nulo");
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "El repositorio de reservas no puede ser nulo");
    }

    /**
     * Registra un nuevo bloqueo operativo en un apartamento verificando previamente
     * que no existan reservas confirmadas o en curso para las fechas solicitadas.
     */
    public void registrarBloqueo(String apartamentoId, Bloqueo bloqueo) {
        Apartamento apartamento = apartamentoRepository.buscarPorId(apartamentoId)
                .orElseThrow(() -> new ReglaDominioException("El apartamento indicado no existe: " + apartamentoId));

        List<Reserva> reservasExistentes = reservaRepository.buscarPorApartamento(apartamentoId);

        boolean hayReservaConflicto = reservasExistentes.stream()
                .filter(r -> r.getEstado() == EstadoReserva.PENDIENTE || r.getEstado() == EstadoReserva.CONFIRMADA || r.getEstado() == EstadoReserva.EN_CURSO)
                .anyMatch(r -> r.getEstancia().seSolapaCon(bloqueo.rango()));

        if (hayReservaConflicto) {
            throw new ReglaDominioException("No se puede registrar el bloqueo: existen reservas activas en las fechas solicitadas.");
        }

        apartamento.agregarBloqueo(bloqueo);
        apartamentoRepository.guardar(apartamento);
    }
}
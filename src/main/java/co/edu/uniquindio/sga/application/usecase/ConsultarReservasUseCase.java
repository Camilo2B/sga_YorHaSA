package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Caso de uso: listar reservas con filtros opcionales, de la más reciente a la más antigua (7.5). */
@Service
public class ConsultarReservasUseCase {

    private final ReservaRepository reservaRepository;

    public ConsultarReservasUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    /** Cada filtro es opcional (null = sin filtrar). */
    @Transactional(readOnly = true)
    public Page<Reserva> ejecutar(EstadoReserva estado, String apartamentoId, LocalDate desde, LocalDate hasta,
                                  Pageable pageable) {
        return Paginacion.paginar(reservaRepository.buscar(estado, apartamentoId, desde, hasta), pageable);
    }
}

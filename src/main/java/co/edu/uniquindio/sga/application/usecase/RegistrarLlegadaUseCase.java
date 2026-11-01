package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.EntregaApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Caso de uso: registrar la llegada del grupo (check-in). La reserva pasa a EN_CURSO y el apartamento a OCUPADO. Las
 * dos modificaciones se guardan en la misma transacción, pero cada agregado conserva sus propias reglas.
 */
@Service
public class RegistrarLlegadaUseCase {

    private final ReservaRepository reservaRepository;
    private final ApartamentoRepository apartamentoRepository;
    private final EntregaApartamentoService entregaApartamentoService;
    private final Clock clock;

    public RegistrarLlegadaUseCase(ReservaRepository reservaRepository, ApartamentoRepository apartamentoRepository,
                                   EntregaApartamentoService entregaApartamentoService, Clock clock) {
        this.reservaRepository = reservaRepository;
        this.apartamentoRepository = apartamentoRepository;
        this.entregaApartamentoService = entregaApartamentoService;
        this.clock = clock;
    }

    @Transactional
    public Reserva ejecutar(CodigoReserva codigo) {
        Reserva reserva = reservaRepository.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        Apartamento apartamento = apartamentoRepository.buscarPorId(reserva.getApartamentoId())
                .orElseThrow(() -> new ApartamentoNoEncontradoException(reserva.getApartamentoId()));

        entregaApartamentoService.autorizarEntrega(reserva, apartamento, LocalDate.now(clock)); // RN-10, RN-11
        reserva.realizarCheckIn();
        apartamento.registrarOcupacion();

        reservaRepository.guardar(reserva);
        apartamentoRepository.guardar(apartamento);
        return reserva;
    }
}

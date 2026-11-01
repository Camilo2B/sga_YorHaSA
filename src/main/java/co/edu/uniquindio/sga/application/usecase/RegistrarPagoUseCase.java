package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/** Caso de uso: registrar un pago contra el folio de una reserva (RN-15). Las reglas viven en Reserva y Folio. */
@Service
public class RegistrarPagoUseCase {

    private final ReservaRepository reservaRepository;
    private final Clock clock;

    public RegistrarPagoUseCase(ReservaRepository reservaRepository, Clock clock) {
        this.reservaRepository = reservaRepository;
        this.clock = clock;
    }

    @Transactional
    public Folio ejecutar(CodigoReserva codigo, Dinero valor, MedioPago medioPago, String referencia) {
        Reserva reserva = reservaRepository.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        reserva.registrarPago(valor, medioPago, referencia, LocalDate.now(clock));
        reservaRepository.guardar(reserva);
        return reserva.getFolio();
    }
}

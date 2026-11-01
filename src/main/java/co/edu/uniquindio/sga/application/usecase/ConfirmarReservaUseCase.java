package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.RecargoLlegadaNocturna;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Caso de uso: confirmar una reserva pendiente con su anticipo. Orquesta; las reglas viven en Reserva. */
@Service
public class ConfirmarReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final RecargoLlegadaNocturna recargoLlegadaNocturna;
    private final Clock clock;

    public ConfirmarReservaUseCase(ReservaRepository reservaRepository, AlojamientoRepository alojamientoRepository,
                                   RecargoLlegadaNocturna recargoLlegadaNocturna, Clock clock) {
        this.reservaRepository = reservaRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.recargoLlegadaNocturna = recargoLlegadaNocturna;
        this.clock = clock;
    }

    /**
     * @param horaEstimadaLlegada solo si la reserva todavía no la tiene registrada (RN-09); si falla la confirmación,
     *                            la transacción se revierte y la hora tampoco queda registrada
     * @param medioPago medio con el que se paga el anticipo; puede ser null solo si el alojamiento no exige anticipo
     */
    @Transactional
    public Reserva ejecutar(CodigoReserva codigo, LocalTime horaEstimadaLlegada, MedioPago medioPago, String referencia) {
        Reserva reserva = reservaRepository.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        Alojamiento alojamiento = alojamientoRepository.obtener().orElseThrow(AlojamientoNoEncontradoException::new);

        if (horaEstimadaLlegada != null) {
            reserva.registrarHoraEstimadaLlegada(horaEstimadaLlegada, recargoLlegadaNocturna);
        }

        reserva.confirmarConAnticipo(alojamiento.parametros().politicaAnticipo(), alojamiento.parametros().plazoConfirmacion(),
                medioPago, referencia, LocalDateTime.now(clock));
        reservaRepository.guardar(reserva);
        return reserva;
    }
}

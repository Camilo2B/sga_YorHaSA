package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.CreacionReservaService;
import co.edu.uniquindio.sga.domain.valueobject.SolicitudReserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/** Caso de uso: crear una reserva. Solo orquesta; todas las reglas de creación viven en CreacionReservaService. */
@Service
public class CrearReservaUseCase {

    private final AlojamientoRepository alojamientoRepository;
    private final ApartamentoRepository apartamentoRepository;
    private final ReservaRepository reservaRepository;
    private final CreacionReservaService creacionReservaService;
    private final Clock clock;

    public CrearReservaUseCase(AlojamientoRepository alojamientoRepository,
                               ApartamentoRepository apartamentoRepository,
                               ReservaRepository reservaRepository,
                               CreacionReservaService creacionReservaService,
                               Clock clock) {
        this.alojamientoRepository = alojamientoRepository;
        this.apartamentoRepository = apartamentoRepository;
        this.reservaRepository = reservaRepository;
        this.creacionReservaService = creacionReservaService;
        this.clock = clock;
    }

    @Transactional
    public Reserva ejecutar(String identificacionApartamento, SolicitudReserva solicitud) {
        Alojamiento alojamiento = alojamientoRepository.obtener().orElseThrow(AlojamientoNoEncontradoException::new);
        Apartamento apartamento = apartamentoRepository.buscarPorId(identificacionApartamento)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacionApartamento));

        Reserva reserva = creacionReservaService.crear(alojamiento, apartamento, solicitud, LocalDateTime.now(clock));
        reservaRepository.guardar(reserva);
        return reserva;
    }
}

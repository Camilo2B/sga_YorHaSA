package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Caso de uso: cancelar automáticamente las reservas PENDIENTE que superaron el plazo de confirmación (RN-21), con lo
 * cual liberan sus noches (RN-12). Lo dispara un proceso programado; no tiene endpoint.
 */
@Service
public class CancelarReservasVencidasUseCase {

    private final ReservaRepository reservaRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final Clock clock;

    public CancelarReservasVencidasUseCase(ReservaRepository reservaRepository, AlojamientoRepository alojamientoRepository, Clock clock) {
        this.reservaRepository = reservaRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.clock = clock;
    }

    /** @return cuántas reservas se cancelaron */
    @Transactional
    public int ejecutar() {
        PlazoConfirmacion plazo = alojamientoRepository.obtener()
                .orElseThrow(AlojamientoNoEncontradoException::new)
                .parametros().plazoConfirmacion();
        LocalDateTime ahora = LocalDateTime.now(clock);

        List<Reserva> vencidas = reservaRepository.buscarPorEstado(EstadoReserva.PENDIENTE).stream()
                .filter(r -> r.haVencidoPlazoConfirmacion(plazo, ahora))
                .toList();
        vencidas.forEach(r -> {
            r.cancelarPorVencimiento(plazo, ahora);
            reservaRepository.guardar(r);
        });
        return vencidas.size();
    }
}

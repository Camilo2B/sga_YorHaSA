package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: registrar la salida del grupo (check-out). La reserva pasa a FINALIZADA solo con el folio en paz y salvo
 * (RN-17) y el apartamento queda PENDIENTE_PREPARACION para el personal de servicio.
 */
@Service
public class RegistrarSalidaUseCase {

    private final ReservaRepository reservaRepository;
    private final ApartamentoRepository apartamentoRepository;

    public RegistrarSalidaUseCase(ReservaRepository reservaRepository, ApartamentoRepository apartamentoRepository) {
        this.reservaRepository = reservaRepository;
        this.apartamentoRepository = apartamentoRepository;
    }

    @Transactional
    public Reserva ejecutar(CodigoReserva codigo) {
        Reserva reserva = reservaRepository.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        Apartamento apartamento = apartamentoRepository.buscarPorId(reserva.getApartamentoId())
                .orElseThrow(() -> new ApartamentoNoEncontradoException(reserva.getApartamentoId()));

        reserva.realizarCheckOut();
        apartamento.registrarDesocupacion();

        reservaRepository.guardar(reserva);
        apartamentoRepository.guardar(apartamento);
        return reserva;
    }
}

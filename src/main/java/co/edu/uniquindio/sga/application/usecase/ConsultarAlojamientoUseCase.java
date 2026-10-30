package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/** Caso de uso: consultar los datos del alojamiento, sus parámetros, temporadas y la política de cancelación vigente (7.3). */
@Service
public class ConsultarAlojamientoUseCase {

    /** El alojamiento junto con la política que rige hoy (la fecha viene del reloj inyectado, no de LocalDate.now()). */
    public record Resultado(Alojamiento alojamiento, PoliticaCancelacion politicaVigente) {
    }

    private final AlojamientoRepository alojamientoRepository;
    private final Clock clock;

    public ConsultarAlojamientoUseCase(AlojamientoRepository alojamientoRepository, Clock clock) {
        this.alojamientoRepository = alojamientoRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Resultado ejecutar() {
        Alojamiento alojamiento = alojamientoRepository.obtener().orElseThrow(AlojamientoNoEncontradoException::new);
        return new Resultado(alojamiento, alojamiento.politicaVigente(LocalDate.now(clock)));
    }
}

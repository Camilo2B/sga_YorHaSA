package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Caso de uso: registrar una temporada (7.4). Orquesta; las reglas (sin solapamiento, una sola base) viven en Alojamiento. */
@Service
public class RegistrarTemporadaUseCase {

    private final AlojamientoRepository alojamientoRepository;

    public RegistrarTemporadaUseCase(AlojamientoRepository alojamientoRepository) {
        this.alojamientoRepository = alojamientoRepository;
    }

    @Transactional
    public Temporada ejecutar(Temporada nueva) {
        Alojamiento alojamiento = alojamientoRepository.obtener().orElseThrow(AlojamientoNoEncontradoException::new);
        alojamiento.agregarTemporada(nueva);
        alojamientoRepository.guardar(alojamiento);
        return nueva;
    }
}

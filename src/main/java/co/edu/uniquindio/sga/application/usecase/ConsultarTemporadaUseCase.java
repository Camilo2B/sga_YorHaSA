package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.TemporadaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Caso de uso: consultar una temporada por su identificador (7.4). */
@Service
public class ConsultarTemporadaUseCase {

    private final AlojamientoRepository alojamientoRepository;

    public ConsultarTemporadaUseCase(AlojamientoRepository alojamientoRepository) {
        this.alojamientoRepository = alojamientoRepository;
    }

    @Transactional(readOnly = true)
    public Temporada ejecutar(UUID id) {
        return alojamientoRepository.obtener()
                .orElseThrow(AlojamientoNoEncontradoException::new)
                .temporada(id)
                .orElseThrow(() -> new TemporadaNoEncontradaException(id));
    }
}

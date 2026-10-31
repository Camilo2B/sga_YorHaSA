package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Caso de uso: consultar todas las versiones de la política de cancelación, de la primera a la más reciente (7.4). */
@Service
public class ListarPoliticasCancelacionUseCase {

    private final AlojamientoRepository alojamientoRepository;

    public ListarPoliticasCancelacionUseCase(AlojamientoRepository alojamientoRepository) {
        this.alojamientoRepository = alojamientoRepository;
    }

    @Transactional(readOnly = true)
    public List<PoliticaCancelacion> ejecutar() {
        return alojamientoRepository.obtener().orElseThrow(AlojamientoNoEncontradoException::new).historialPoliticas();
    }
}

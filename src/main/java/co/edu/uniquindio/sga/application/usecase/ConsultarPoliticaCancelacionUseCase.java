package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.PoliticaCancelacionNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Caso de uso: consultar una versión de la política de cancelación por su número (7.4). */
@Service
public class ConsultarPoliticaCancelacionUseCase {

    private final AlojamientoRepository alojamientoRepository;

    public ConsultarPoliticaCancelacionUseCase(AlojamientoRepository alojamientoRepository) {
        this.alojamientoRepository = alojamientoRepository;
    }

    @Transactional(readOnly = true)
    public PoliticaCancelacion ejecutar(int version) {
        return alojamientoRepository.obtener()
                .orElseThrow(AlojamientoNoEncontradoException::new)
                .politicaPorNumero(version)
                .orElseThrow(() -> new PoliticaCancelacionNoEncontradaException("versión " + version));
    }
}

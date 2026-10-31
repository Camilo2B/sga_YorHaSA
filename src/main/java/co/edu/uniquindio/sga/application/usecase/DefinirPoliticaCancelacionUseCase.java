package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso: definir una nueva versión de la política de cancelación (F-07, 7.4). Orquesta; las reglas (mínimo
 * dos tramos, tramo de 0 horas, vigencia posterior a la versión más reciente) viven en el dominio.
 */
@Service
public class DefinirPoliticaCancelacionUseCase {

    private final AlojamientoRepository alojamientoRepository;

    public DefinirPoliticaCancelacionUseCase(AlojamientoRepository alojamientoRepository) {
        this.alojamientoRepository = alojamientoRepository;
    }

    @Transactional
    public PoliticaCancelacion ejecutar(List<TramoAntelacion> tramos, LocalDate vigenteDesde) {
        Alojamiento alojamiento = alojamientoRepository.obtener().orElseThrow(AlojamientoNoEncontradoException::new);
        PoliticaCancelacion nueva = alojamiento.definirNuevaPolitica(tramos, vigenteDesde);
        alojamientoRepository.guardar(alojamiento);
        return nueva;
    }
}

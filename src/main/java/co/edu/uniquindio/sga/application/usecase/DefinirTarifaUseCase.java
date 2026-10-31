package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.TemporadaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Caso de uso: definir la tarifa de un apartamento para una temporada (7.4). Orquesta: comprueba que existan el
 * apartamento y la temporada (agregados distintos, por eso se consultan por separado); la regla de que la nueva
 * tarifa rija después de la anterior vive en Apartamento.definirTarifa.
 */
@Service
public class DefinirTarifaUseCase {

    private final ApartamentoRepository apartamentoRepository;
    private final AlojamientoRepository alojamientoRepository;

    public DefinirTarifaUseCase(ApartamentoRepository apartamentoRepository, AlojamientoRepository alojamientoRepository) {
        this.apartamentoRepository = apartamentoRepository;
        this.alojamientoRepository = alojamientoRepository;
    }

    @Transactional
    public Tarifa ejecutar(String identificacionApartamento, UUID temporadaId, Dinero valorPorNoche, LocalDate vigenteDesde) {
        Apartamento apartamento = apartamentoRepository.buscarPorId(identificacionApartamento)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacionApartamento));
        alojamientoRepository.obtener()
                .orElseThrow(AlojamientoNoEncontradoException::new)
                .temporada(temporadaId)
                .orElseThrow(() -> new TemporadaNoEncontradaException(temporadaId));
        Tarifa nueva = apartamento.definirTarifa(temporadaId, valorPorNoche, vigenteDesde);
        apartamentoRepository.guardar(apartamento);
        return nueva;
    }
}

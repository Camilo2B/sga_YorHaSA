package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.TarifaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Caso de uso: consultar una tarifa del histórico de un apartamento (7.4). */
@Service
public class ConsultarTarifaUseCase {

    private final ApartamentoRepository apartamentoRepository;

    public ConsultarTarifaUseCase(ApartamentoRepository apartamentoRepository) {
        this.apartamentoRepository = apartamentoRepository;
    }

    @Transactional(readOnly = true)
    public Tarifa ejecutar(String identificacionApartamento, UUID idTarifa) {
        return apartamentoRepository.buscarPorId(identificacionApartamento)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacionApartamento))
                .tarifa(idTarifa)
                .orElseThrow(() -> new TarifaNoEncontradaException(identificacionApartamento, idTarifa));
    }
}

package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Caso de uso: consultar el histórico de tarifas de un apartamento, opcionalmente de una sola temporada (7.4). */
@Service
public class ListarTarifasApartamentoUseCase {

    private final ApartamentoRepository apartamentoRepository;

    public ListarTarifasApartamentoUseCase(ApartamentoRepository apartamentoRepository) {
        this.apartamentoRepository = apartamentoRepository;
    }

    /** @param temporadaId si es null se devuelven las tarifas de todas las temporadas */
    @Transactional(readOnly = true)
    public Page<Tarifa> ejecutar(String identificacionApartamento, UUID temporadaId, Pageable pageable) {
        Apartamento apartamento = apartamentoRepository.buscarPorId(identificacionApartamento)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacionApartamento));
        return Paginacion.paginar(
                temporadaId == null ? apartamento.historialTarifas() : apartamento.historialTarifas(temporadaId),
                pageable);
    }
}

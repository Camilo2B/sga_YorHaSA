package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Caso de uso: consultar un apartamento por su identificación (7.3). */
@Service
public class ConsultarApartamentoUseCase {

    private final ApartamentoRepository apartamentoRepository;

    public ConsultarApartamentoUseCase(ApartamentoRepository apartamentoRepository) {
        this.apartamentoRepository = apartamentoRepository;
    }

    @Transactional(readOnly = true)
    public Apartamento ejecutar(String identificacion) {
        return apartamentoRepository.buscarPorId(identificacion)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacion));
    }
}

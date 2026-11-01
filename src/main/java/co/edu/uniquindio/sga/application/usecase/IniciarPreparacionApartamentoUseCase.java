package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Caso de uso: el personal de servicio empieza a preparar el apartamento (PENDIENTE_PREPARACION -> EN_PREPARACION, 7.6). */
@Service
public class IniciarPreparacionApartamentoUseCase {

    private final ApartamentoRepository apartamentoRepository;

    public IniciarPreparacionApartamentoUseCase(ApartamentoRepository apartamentoRepository) {
        this.apartamentoRepository = apartamentoRepository;
    }

    @Transactional
    public Apartamento ejecutar(String identificacion) {
        Apartamento apartamento = apartamentoRepository.buscarPorId(identificacion)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacion));
        apartamento.iniciarPreparacion();
        apartamentoRepository.guardar(apartamento);
        return apartamento;
    }
}

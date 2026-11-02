package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.service.RegistroApartamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Caso de uso: cambiar la capacidad de un apartamento (7.3). Orquesta; las reglas viven en el dominio. */
@Service
public class CambiarCapacidadApartamentoUseCase {

    private final ApartamentoRepository apartamentoRepository;
    private final RegistroApartamentoService registroApartamentoService;

    public CambiarCapacidadApartamentoUseCase(ApartamentoRepository apartamentoRepository,
                                              RegistroApartamentoService registroApartamentoService) {
        this.apartamentoRepository = apartamentoRepository;
        this.registroApartamentoService = registroApartamentoService;
    }

    @Transactional
    public Apartamento ejecutar(String identificacion, int nuevaCapacidad) {
        Apartamento apartamento = apartamentoRepository.buscarPorId(identificacion)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacion));
        registroApartamentoService.cambiarCapacidad(apartamento, nuevaCapacidad);
        apartamentoRepository.guardar(apartamento);
        return apartamento;
    }
}

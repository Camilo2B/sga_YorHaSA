package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.service.RegistroApartamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Caso de uso: retirar un apartamento de la venta (7.3, eliminación lógica 12.4). Orquesta; la regla vive en el dominio. */
@Service
public class RetirarApartamentoUseCase {

    private final ApartamentoRepository apartamentoRepository;
    private final RegistroApartamentoService registroApartamentoService;

    public RetirarApartamentoUseCase(ApartamentoRepository apartamentoRepository,
                                     RegistroApartamentoService registroApartamentoService) {
        this.apartamentoRepository = apartamentoRepository;
        this.registroApartamentoService = registroApartamentoService;
    }

    @Transactional
    public Apartamento ejecutar(String identificacion) {
        Apartamento apartamento = apartamentoRepository.buscarPorId(identificacion)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacion));
        registroApartamentoService.retirar(apartamento);
        apartamentoRepository.guardar(apartamento);
        return apartamento;
    }
}

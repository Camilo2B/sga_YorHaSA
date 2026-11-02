package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.BloqueoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Caso de uso: consultar un bloqueo de un apartamento (RN-07). */
@Service
public class ConsultarBloqueoUseCase {

    private final ApartamentoRepository apartamentoRepository;

    public ConsultarBloqueoUseCase(ApartamentoRepository apartamentoRepository) {
        this.apartamentoRepository = apartamentoRepository;
    }

    @Transactional(readOnly = true)
    public Bloqueo ejecutar(String identificacionApartamento, UUID idBloqueo) {
        return apartamentoRepository.buscarPorId(identificacionApartamento)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacionApartamento))
                .bloqueo(idBloqueo)
                .orElseThrow(() -> new BloqueoNoEncontradoException(identificacionApartamento, idBloqueo));
    }
}

package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

/** Caso de uso: listar los bloqueos de un apartamento, ordenados por fecha de inicio (RN-07). */
@Service
public class ListarBloqueosApartamentoUseCase {

    private final ApartamentoRepository apartamentoRepository;

    public ListarBloqueosApartamentoUseCase(ApartamentoRepository apartamentoRepository) {
        this.apartamentoRepository = apartamentoRepository;
    }

    @Transactional(readOnly = true)
    public Page<Bloqueo> ejecutar(String identificacionApartamento, Pageable pageable) {
        return Paginacion.paginar(apartamentoRepository.buscarPorId(identificacionApartamento)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacionApartamento))
                .bloqueos().stream()
                .sorted(Comparator.comparing(b -> b.rango().entrada()))
                .toList(), pageable);
    }
}

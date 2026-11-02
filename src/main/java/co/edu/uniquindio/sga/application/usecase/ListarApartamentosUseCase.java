package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

/** Caso de uso: listar el inventario de apartamentos (activos y retirados), ordenado por identificación (7.3). */
@Service
public class ListarApartamentosUseCase {

    private final ApartamentoRepository apartamentoRepository;

    public ListarApartamentosUseCase(ApartamentoRepository apartamentoRepository) {
        this.apartamentoRepository = apartamentoRepository;
    }

    @Transactional(readOnly = true)
    public Page<Apartamento> ejecutar(Pageable pageable) {
        return Paginacion.paginar(apartamentoRepository.obtenerTodos().stream()
                .sorted(Comparator.comparing(Apartamento::identificacion))
                .toList(), pageable);
    }
}

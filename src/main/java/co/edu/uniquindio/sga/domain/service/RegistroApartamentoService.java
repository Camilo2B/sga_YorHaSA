package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;

import java.util.Objects;

/**
 * L-04 / 7.3: la identificación de cada apartamento es única y estable.
 * <p>
 * Es servicio de dominio -y no método de Apartamento- porque verificar que NO exista otro
 * apartamento con esa identificación requiere consultar al repositorio, es decir, mirar fuera
 * del agregado: ningún Apartamento puede evaluarlo solo.
 */
public class RegistroApartamentoService {

    private final ApartamentoRepository apartamentoRepository;

    public RegistroApartamentoService(ApartamentoRepository apartamentoRepository) {
        this.apartamentoRepository = Objects.requireNonNull(apartamentoRepository);
    }

    public void registrar(Apartamento apartamento) {
        Objects.requireNonNull(apartamento, "El apartamento no puede ser nulo");
        if (apartamentoRepository.buscarPorId(apartamento.identificacion()).isPresent()) {
            throw new ReglaDominioException(
                    "Ya existe un apartamento con la identificación " + apartamento.identificacion() + " (L-04)");
        }
        apartamentoRepository.guardar(apartamento);
    }
}

package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Apartamento;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para el agregado Apartamento.
 */
public interface ApartamentoRepository {

    void guardar(Apartamento apartamento);

    Optional<Apartamento> buscarPorId(String identificacion);

    List<Apartamento> obtenerTodos();

    void eliminar(String identificacion);
}
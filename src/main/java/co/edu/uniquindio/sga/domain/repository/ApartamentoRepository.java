package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Apartamento;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para el agregado Apartamento.
 * <p>
 * No hay "eliminar": 12.4 exige eliminación lógica, que se hace con Apartamento.desactivar()
 * y un guardar(...) posterior; las reservas históricas siguen referenciando al apartamento.
 */
public interface ApartamentoRepository {

    void guardar(Apartamento apartamento);

    Optional<Apartamento> buscarPorId(String identificacion);

    List<Apartamento> obtenerTodos();
}

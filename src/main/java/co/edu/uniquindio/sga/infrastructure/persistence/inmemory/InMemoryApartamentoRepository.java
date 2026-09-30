package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria usando HashMap exigida por la rúbrica para la Entrega 1.
 */
public class InMemoryApartamentoRepository implements ApartamentoRepository {

    private final Map<String, Apartamento> tablaApartamentos = new HashMap<>();

    @Override
    public void guardar(Apartamento apartamento) {
        tablaApartamentos.put(apartamento.identificacion(), apartamento);
    }

    @Override
    public Optional<Apartamento> buscarPorId(String identificacion) {
        return Optional.ofNullable(tablaApartamentos.get(identificacion));
    }

    @Override
    public List<Apartamento> obtenerTodos() {
        return new ArrayList<>(tablaApartamentos.values());
    }

    @Override
    public void eliminar(String identificacion) {
        tablaApartamentos.remove(identificacion);
    }
}
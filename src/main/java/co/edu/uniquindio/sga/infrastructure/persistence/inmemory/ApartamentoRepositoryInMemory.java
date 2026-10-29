package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria (HashMap) exigida por la rúbrica de la Entrega 1. Ya no es un bean de Spring:
 * la aplicación usa el adaptador JPA; esta clase queda solo para pruebas unitarias del dominio.
 */
public class ApartamentoRepositoryInMemory implements ApartamentoRepository {

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
}

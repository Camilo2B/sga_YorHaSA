package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Profile("inmemory")
public class ApartamentoRepositoryInMemory implements ApartamentoRepository {

    private final Map<String, Apartamento> repositorio = new HashMap<>();

    @Override
    public void guardar(Apartamento apartamento) {
        repositorio.put(apartamento.identificacion(), apartamento);
    }

    @Override
    public Optional<Apartamento> buscarPorId(String identificacion) {
        return Optional.ofNullable(repositorio.get(identificacion));
    }

    @Override
    public List<Apartamento> obtenerTodos() {
        return repositorio.values().stream().toList();
    }
}

package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ApartamentoPersistenceMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Adaptador JPA del puerto ApartamentoRepository. La transacción mantiene abierta la sesión mientras se arma el dominio. */
@Repository
@Transactional
public class ApartamentoJpaRepository implements ApartamentoRepository {

    private final ApartamentoJpaDataRepository dataRepository;
    private final ApartamentoPersistenceMapper mapper;

    public ApartamentoJpaRepository(ApartamentoJpaDataRepository dataRepository, ApartamentoPersistenceMapper mapper) {
        this.dataRepository = dataRepository;
        this.mapper = mapper;
    }

    @Override
    public void guardar(Apartamento apartamento) {
        dataRepository.save(mapper.toEntity(apartamento));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Apartamento> buscarPorId(String identificacion) {
        return dataRepository.findById(identificacion).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Apartamento> obtenerTodos() {
        return dataRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}

package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosAlojamiento;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.AlojamientoPersistenceMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptador JPA del puerto AlojamientoRepository. Los parámetros de la Ficha no están en la base: se inyectan desde
 * la configuración y se entregan al dominio al reconstruirlo.
 */
@Repository
@Transactional
public class AlojamientoJpaRepository implements AlojamientoRepository {

    private final AlojamientoJpaDataRepository dataRepository;
    private final AlojamientoPersistenceMapper mapper;
    private final ParametrosAlojamiento parametros;

    public AlojamientoJpaRepository(AlojamientoJpaDataRepository dataRepository,
                                    AlojamientoPersistenceMapper mapper,
                                    ParametrosAlojamiento parametros) {
        this.dataRepository = dataRepository;
        this.mapper = mapper;
        this.parametros = parametros;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Alojamiento> obtener() {
        // F-13: existe un único alojamiento
        return dataRepository.findAll().stream().findFirst().map(entity -> mapper.toDomain(entity, parametros));
    }

    @Override
    public void guardar(Alojamiento alojamiento) {
        dataRepository.save(mapper.toEntity(alojamiento));
    }
}

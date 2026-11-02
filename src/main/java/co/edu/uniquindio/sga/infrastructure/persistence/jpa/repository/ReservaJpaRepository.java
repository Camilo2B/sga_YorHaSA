package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ReservaPersistenceMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Adaptador JPA del puerto ReservaRepository: es el que usa la aplicación (el de memoria queda solo para pruebas). */
@Repository
@Transactional
public class ReservaJpaRepository implements ReservaRepository {

    private final ReservaJpaDataRepository dataRepository;
    private final ReservaPersistenceMapper mapper;

    public ReservaJpaRepository(ReservaJpaDataRepository dataRepository, ReservaPersistenceMapper mapper) {
        this.dataRepository = dataRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> buscarActivasPorApartamento(String apartamentoId, Estancia estancia) {
        return dataRepository.buscarSolapadas(apartamentoId, estadosActivos(), estancia.entrada(), estancia.salida())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> buscarActivasPorApartamento(String apartamentoId) {
        return dataRepository.findByApartamentoIdAndEstadoIn(apartamentoId, estadosActivos())
                .stream().map(mapper::toDomain).toList();
    }

    /** Qué estados son "activos" lo decide el dominio (EstadoReserva.esActiva), no la consulta. */
    private static List<EstadoReserva> estadosActivos() {
        return Arrays.stream(EstadoReserva.values()).filter(EstadoReserva::esActiva).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reserva> obtenerPorCodigo(CodigoReserva codigo) {
        return dataRepository.findByCodigo(codigo.valor()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> buscarPorEstado(EstadoReserva estado) {
        return dataRepository.findByEstado(estado).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> buscar(EstadoReserva estado, String apartamentoId, LocalDate desde, LocalDate hasta) {
        // Los filtros se resuelven en la consulta; solo se agregan los que llegan (null = sin filtrar).
        Specification<ReservaEntity> filtros = (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (estado != null) {
                condiciones.add(cb.equal(root.get("estado"), estado));
            }
            if (apartamentoId != null) {
                condiciones.add(cb.equal(root.get("apartamentoId"), apartamentoId));
            }
            if (desde != null) {
                condiciones.add(cb.greaterThan(root.get("estancia").get("salida"), desde));
            }
            if (hasta != null) {
                condiciones.add(cb.lessThan(root.get("estancia").get("entrada"), hasta));
            }
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
        return dataRepository.findAll(filtros, Sort.by(Sort.Direction.DESC, "fechaCreacion"))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public void guardar(Reserva reserva) {
        dataRepository.save(mapper.toEntity(reserva));
    }
}

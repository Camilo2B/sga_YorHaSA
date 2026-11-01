package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ReservaPersistenceMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
        // Qué estados son "activos" lo decide el dominio (EstadoReserva.esActiva), no la consulta.
        List<EstadoReserva> estadosActivos = Arrays.stream(EstadoReserva.values()).filter(EstadoReserva::esActiva).toList();
        return dataRepository.buscarSolapadas(apartamentoId, estadosActivos, estancia.entrada(), estancia.salida())
                .stream().map(mapper::toDomain).toList();
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
    public void guardar(Reserva reserva) {
        dataRepository.save(mapper.toEntity(reserva));
    }
}

package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repositorio Spring Data: detalle interno de infraestructura, el dominio nunca lo ve. */
public interface ReservaJpaDataRepository extends JpaRepository<ReservaEntity, UUID>, JpaSpecificationExecutor<ReservaEntity> {

    Optional<ReservaEntity> findByCodigo(String codigo);

    List<ReservaEntity> findByEstado(EstadoReserva estado);

    List<ReservaEntity> findByApartamentoIdAndEstadoIn(String apartamentoId, Collection<EstadoReserva> estados);

    /** Cuántas reservas hay con un código que empieza por el prefijo (RES-AAAA-), para calcular el siguiente consecutivo. */
    long countByCodigoStartingWith(String prefijo);

    /** El solapamiento [entrada, salida) se resuelve en la consulta: entrada_A < salida_B y entrada_B < salida_A (3.1). */
    @Query("""
            select r from ReservaEntity r
            where r.apartamentoId = :apartamentoId
              and r.estado in :estados
              and r.estancia.entrada < :salida
              and r.estancia.salida > :entrada
            """)
    List<ReservaEntity> buscarSolapadas(@Param("apartamentoId") String apartamentoId,
                                        @Param("estados") Collection<EstadoReserva> estados,
                                        @Param("entrada") LocalDate entrada,
                                        @Param("salida") LocalDate salida);
}

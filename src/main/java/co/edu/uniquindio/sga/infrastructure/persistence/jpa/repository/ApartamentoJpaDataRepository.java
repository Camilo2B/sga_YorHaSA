package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ApartamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data: detalle interno de infraestructura, el dominio nunca lo ve. */
public interface ApartamentoJpaDataRepository extends JpaRepository<ApartamentoEntity, String> {
}

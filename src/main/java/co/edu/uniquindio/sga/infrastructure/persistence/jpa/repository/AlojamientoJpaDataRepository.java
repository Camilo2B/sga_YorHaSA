package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.AlojamientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Repositorio Spring Data: detalle interno de infraestructura, el dominio nunca lo ve. */
public interface AlojamientoJpaDataRepository extends JpaRepository<AlojamientoEntity, UUID> {
}

package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import co.edu.uniquindio.sga.infrastructure.security.RolUsuario;

/** Repositorio Spring Data de usuarios (Guía 12) */
public interface UsuarioJpaDataRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByCorreo(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByDocumento(String documento);

    boolean existsByCorreo(String correo);

    List<UsuarioEntity> findAllByRolInOrderByNombreAsc(Collection<RolUsuario> roles);
}
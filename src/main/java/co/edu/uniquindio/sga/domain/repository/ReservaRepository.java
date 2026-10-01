package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Reserva;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrato de persistencia para el agregado Reserva.
 */
public interface ReservaRepository {

    void guardar(Reserva reserva);

    Optional<Reserva> buscarPorId(UUID id);

    List<Reserva> buscarPorApartamento(String apartamentoId);

    List<Reserva> obtenerTodas();
}
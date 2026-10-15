package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.util.List;

public interface ReservaRepository {

    List<Reserva> buscarActivasByApartamento(identificacionApartamento apartamento);
}

package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository {

    /**
     * Reservas activas (PENDIENTE, CONFIRMADA o EN_CURSO) del apartamento cuya estancia se solapa con la indicada.
     * El filtro por fechas vive en el puerto para que, con base de datos, se resuelva en la consulta y no
     * cargando todo el historial del apartamento.
     */
    List<Reserva> buscarActivasPorApartamento(String apartamentoId, Estancia estancia);

    /** Reservas activas (PENDIENTE, CONFIRMADA o EN_CURSO) del apartamento en cualquier fecha: las vigentes y las futuras. */
    List<Reserva> buscarActivasPorApartamento(String apartamentoId);

    Optional<Reserva> obtenerPorCodigo(CodigoReserva codigo);

    List<Reserva> buscarPorEstado(EstadoReserva estado);

    /**
     * 7.5: listado de reservas de la más reciente a la más antigua (por fecha de creación). Cada filtro es opcional
     * (null = sin filtrar); desde y hasta traen las reservas cuya estancia tiene al menos una noche en [desde, hasta).
     */
    List<Reserva> buscar(EstadoReserva estado, String apartamentoId, LocalDate desde, LocalDate hasta);

    void guardar(Reserva reserva);
}

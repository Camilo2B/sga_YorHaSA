package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository {

    /**
     * Reservas activas (PENDIENTE, CONFIRMADA o EN_CURSO) del apartamento cuya estancia se solapa con la indicada.
     * El filtro por fechas vive en el puerto para que, con base de datos, se resuelva en la consulta y no
     * cargando todo el historial del apartamento.
     */
    List<Reserva> buscarActivasPorApartamento(String apartamentoId, Estancia estancia);

    Optional<Reserva> obtenerPorCodigo(CodigoReserva codigo);

    List<Reserva> buscarPorEstado(EstadoReserva estado);

    void guardar(Reserva reserva);
}

package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Servicio de dominio: decide SI se puede entregar el apartamento a una reserva. Lo necesita porque Reserva no puede
 * leer el estado operativo del Apartamento (es una precondición externa, no una invariante suya). Este servicio
 * decide si se entrega; Reserva y Apartamento deciden cómo cambian de estado.
 * <ul>
 *   <li>RN-10: el registro solo es sobre una reserva CONFIRMADA, no antes de la fecha de entrada, ni cuando la
 *       estancia ya terminó.</li>
 *   <li>RN-11: el apartamento debe estar PREPARADO.</li>
 * </ul>
 * Clase Java pura: se registra como bean en DomainServiceConfig.
 */
public class EntregaApartamentoService {

    public void autorizarEntrega(Reserva reserva, Apartamento apartamento, LocalDate hoy) {
        Objects.requireNonNull(reserva, "La reserva no puede ser nula");
        Objects.requireNonNull(apartamento, "El apartamento no puede ser nulo");
        Objects.requireNonNull(hoy, "La fecha actual no puede ser nula");

        if (!reserva.getApartamentoId().equals(apartamento.identificacion())) {
            throw new ReglaDominioException("La reserva " + reserva.getCodigo().valor() + " no corresponde al apartamento "
                    + apartamento.identificacion());
        }
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ReglaDominioException("RN-10: el registro de llegada solo se hace sobre una reserva CONFIRMADA (estado actual: "
                    + reserva.getEstado() + ")");
        }
        if (hoy.isBefore(reserva.getEstancia().entrada())) {
            throw new ReglaDominioException("RN-10: no se permite el registro antes de la fecha de entrada ("
                    + reserva.getEstancia().entrada() + ")");
        }
        if (!hoy.isBefore(reserva.getEstancia().salida())) {
            throw new ReglaDominioException("RN-10: la estancia ya terminó el " + reserva.getEstancia().salida()
                    + "; no se puede registrar la llegada");
        }
        if (!apartamento.estadoOperativo().permiteRegistro()) {
            throw new ReglaDominioException("RN-11: el apartamento " + apartamento.identificacion()
                    + " debe estar PREPARADO para recibir a un grupo (estado actual: " + apartamento.estadoOperativo() + ")");
        }
    }
}

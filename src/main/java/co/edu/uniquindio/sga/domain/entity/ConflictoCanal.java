package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * RN-18: intento de reserva de un canal externo que choca con una reserva vigente. Referencia a la reserva vigente
 * y al apartamento solo por su identificador; la reserva externa rechazada se identifica por su id en el canal.
 */
public final class ConflictoCanal {

    private final UUID id;
    private final String idReservaExterna;
    private final CodigoReserva reservaVigente;
    private final String apartamentoId;
    private final LocalDateTime fechaHoraIntento;
    private final String observacion;
    private boolean revisado;

    private ConflictoCanal(UUID id, String idReservaExterna, CodigoReserva reservaVigente, String apartamentoId,
                           LocalDateTime fechaHoraIntento, String observacion, boolean revisado) {
        this.id = id;
        this.idReservaExterna = idReservaExterna;
        this.reservaVigente = reservaVigente;
        this.apartamentoId = apartamentoId;
        this.fechaHoraIntento = fechaHoraIntento;
        this.observacion = observacion;
        this.revisado = revisado;
    }

    public static ConflictoCanal registrar(String idReservaExterna, CodigoReserva reservaVigente, String apartamentoId,
                                           LocalDateTime fechaHoraIntento, String observacion) {
        if (idReservaExterna == null || idReservaExterna.isBlank()) {
            throw new ReglaDominioException("La reserva externa no puede ser vacia");
        }
        if (reservaVigente == null) {
            throw new ReglaDominioException("La reserva Vigente no puede ser vacia");
        }
        if (apartamentoId == null || apartamentoId.isBlank()) {
            throw new ReglaDominioException("El id del apartamento no puede ser vacio");
        }
        if (fechaHoraIntento == null) {
            throw new ReglaDominioException("La fecha y hora del intento no puede ser vacio");
        }
        if (observacion == null) {
            throw new ReglaDominioException("La observación no puede ser vacia");
        }
        return new ConflictoCanal(UUID.randomUUID(), idReservaExterna, reservaVigente, apartamentoId, fechaHoraIntento,
                observacion, false);
    }

    /** El conflicto fue revisado por el personal; no se puede revisar dos veces. */
    public void marcarRevisado() {
        if (revisado) {
            throw new ReglaDominioException("El conflicto de canal ya fue revisado");
        }
        this.revisado = true;
    }

    public UUID getId() {
        return id;
    }

    public String getIdReservaExterna() {
        return idReservaExterna;
    }

    public CodigoReserva getReservaVigente() {
        return reservaVigente;
    }

    public String getApartamentoId() {
        return apartamentoId;
    }

    public LocalDateTime getFechaHoraIntento() {
        return fechaHoraIntento;
    }

    public String getObservacion() {
        return observacion;
    }

    public boolean isRevisado() {
        return revisado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConflictoCanal otro)) return false;
        return Objects.equals(id, otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

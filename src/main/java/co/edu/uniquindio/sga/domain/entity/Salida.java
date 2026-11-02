package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Salida (check-out) de un grupo. Referencia a la reserva, al apartamento y al titular solo por su identificador:
 * son otros agregados (o parte de ellos), y un agregado nunca guarda a otro.
 */
public final class Salida {

    private final UUID id;
    private final LocalDate fechaSalida;
    private final LocalTime horaSalida;
    private final CodigoReserva codigoReserva;
    private final String apartamentoId;
    private final String documentoTitular;

    private Salida(UUID id, LocalDate fechaSalida, LocalTime horaSalida, CodigoReserva codigoReserva,
                   String apartamentoId, String documentoTitular) {
        this.id = id;
        this.fechaSalida = fechaSalida;
        this.horaSalida = horaSalida;
        this.codigoReserva = codigoReserva;
        this.apartamentoId = apartamentoId;
        this.documentoTitular = documentoTitular;
    }

    public static Salida crear(LocalDate fechaSalida, LocalTime horaSalida, CodigoReserva codigoReserva,
                               String apartamentoId, String documentoTitular) {
        if (fechaSalida == null) {
            throw new ReglaDominioException("La fecha de salida no puede ser vacia");
        }
        if (horaSalida == null) {
            throw new ReglaDominioException("La hora de salida no puede ser vacia");
        }
        if (codigoReserva == null) {
            throw new ReglaDominioException("La reserva no puede ser vacia");
        }
        if (apartamentoId == null || apartamentoId.isBlank()) {
            throw new ReglaDominioException("El apartamento no puede ser vacio");
        }
        if (documentoTitular == null || documentoTitular.isBlank()) {
            throw new ReglaDominioException("El titular no puede ser vacio");
        }
        return new Salida(UUID.randomUUID(), fechaSalida, horaSalida, codigoReserva, apartamentoId, documentoTitular);
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public CodigoReserva getCodigoReserva() {
        return codigoReserva;
    }

    public String getApartamentoId() {
        return apartamentoId;
    }

    public String getDocumentoTitular() {
        return documentoTitular;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Salida salida)) return false;
        return Objects.equals(id, salida.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

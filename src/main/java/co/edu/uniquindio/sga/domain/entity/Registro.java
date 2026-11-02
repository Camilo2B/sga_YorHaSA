package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Registro (check-in) de un grupo. Referencia a la reserva, al apartamento y al titular solo por su identificador:
 * son otros agregados (o parte de ellos), y un agregado nunca guarda a otro.
 */
public final class Registro {

    private final UUID id;
    private final LocalDate fechaEntrada;
    private final LocalTime horaEntrada;
    private final CodigoReserva codigoReserva;
    private final String apartamentoId;
    private final String documentoTitular;

    private Registro(UUID id, LocalDate fechaEntrada, LocalTime horaEntrada, CodigoReserva codigoReserva,
                     String apartamentoId, String documentoTitular) {
        this.id = id;
        this.fechaEntrada = fechaEntrada;
        this.horaEntrada = horaEntrada;
        this.codigoReserva = codigoReserva;
        this.apartamentoId = apartamentoId;
        this.documentoTitular = documentoTitular;
    }

    public static Registro crear(LocalDate fechaEntrada, LocalTime horaEntrada, CodigoReserva codigoReserva,
                                 String apartamentoId, String documentoTitular) {
        if (fechaEntrada == null) {
            throw new ReglaDominioException("La fecha de entrada no puede ser vacia");
        }
        if (horaEntrada == null) {
            throw new ReglaDominioException("La hora de entrada no puede ser vacia");
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
        return new Registro(UUID.randomUUID(), fechaEntrada, horaEntrada, codigoReserva, apartamentoId, documentoTitular);
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
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
        if (!(o instanceof Registro registro)) return false;
        return Objects.equals(id, registro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

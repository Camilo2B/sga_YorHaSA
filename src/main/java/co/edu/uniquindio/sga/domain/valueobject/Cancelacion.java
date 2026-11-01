package co.edu.uniquindio.sga.domain.valueobject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Lo que queda registrado cuando una reserva se cancela: el motivo, cuándo, y cómo se repartió el anticipo cobrado
 * entre lo que se retiene y lo que se devuelve (RN-13). Retenido más devuelto es siempre igual a lo pagado.
 */
public record Cancelacion(String motivo, LocalDateTime fecha, Dinero retenido, Dinero devuelto) {

    public Cancelacion {
        Objects.requireNonNull(motivo, "El motivo de la cancelación no puede ser nulo");
        Objects.requireNonNull(fecha, "La fecha de la cancelación no puede ser nula");
        Objects.requireNonNull(retenido, "El valor retenido no puede ser nulo");
        Objects.requireNonNull(devuelto, "El valor devuelto no puede ser nulo");
    }
}

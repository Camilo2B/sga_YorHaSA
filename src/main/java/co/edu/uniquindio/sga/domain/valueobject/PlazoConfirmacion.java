package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;
import java.util.Objects;

/** L-14: horas que se espera la confirmación de una reserva PENDIENTE antes de cancelarla (RN-21). */
public record PlazoConfirmacion(int horas) {

    public PlazoConfirmacion {
        if (horas <= 0) {
            throw new ReglaDominioException("El plazo de confirmación debe ser mayor a cero horas");
        }
    }

    /** RN-21: una reserva pendiente que SUPERA el plazo se cancela automáticamente. */
    public boolean haVencido(LocalDateTime creadaEn, LocalDateTime ahora) {
        Objects.requireNonNull(creadaEn, "El momento de creación no puede ser nulo");
        Objects.requireNonNull(ahora, "El momento actual no puede ser nulo");
        return ahora.isAfter(creadaEn.plusHours(horas));
    }
}

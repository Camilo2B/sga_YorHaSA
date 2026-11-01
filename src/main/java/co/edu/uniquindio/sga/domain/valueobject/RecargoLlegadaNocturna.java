package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalTime;
import java.util.Objects;

/**
 * RN-Propia-02 (Recargo por Despliegue Nocturno): una llegada estimada dentro de la franja nocturna genera un cargo
 * fijo en el folio. El valor y la franja son parámetros del alojamiento (application.properties). La franja puede
 * cruzar la medianoche (por ejemplo de 22:00 a 05:00) y incluye ambos extremos.
 */
public record RecargoLlegadaNocturna(Dinero valor, LocalTime desde, LocalTime hasta) {

    public RecargoLlegadaNocturna {
        Objects.requireNonNull(valor, "El valor del recargo no puede ser nulo");
        Objects.requireNonNull(desde, "La hora de inicio de la franja nocturna no puede ser nula");
        Objects.requireNonNull(hasta, "La hora de fin de la franja nocturna no puede ser nula");
        if (valor.esCero()) {
            throw new ReglaDominioException("El recargo por llegada nocturna debe ser mayor a cero");
        }
        if (desde.equals(hasta)) {
            throw new ReglaDominioException("La franja de llegada nocturna debe tener inicio y fin distintos");
        }
    }

    public boolean aplicaA(LocalTime horaLlegada) {
        Objects.requireNonNull(horaLlegada, "La hora de llegada no puede ser nula");
        if (desde.isBefore(hasta)) {
            return !horaLlegada.isBefore(desde) && !horaLlegada.isAfter(hasta);
        }
        return !horaLlegada.isBefore(desde) || !horaLlegada.isAfter(hasta); // cruza la medianoche
    }
}

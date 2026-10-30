package co.edu.uniquindio.sga.domain.valueobject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/** L-15: hora del día de entrada a partir de la cual se puede declarar no-show (7.5). */
public record HoraLimiteNoShow(LocalTime hora) {

    public HoraLimiteNoShow {
        Objects.requireNonNull(hora, "La hora límite de no-show no puede ser nula");
    }

    /** 7.5: el no-show puede declararse a partir de la hora límite del día de entrada (ese instante ya cuenta). */
    public boolean permiteDeclarar(LocalDate fechaEntrada, LocalDateTime ahora) {
        Objects.requireNonNull(fechaEntrada, "La fecha de entrada no puede ser nula");
        Objects.requireNonNull(ahora, "El momento actual no puede ser nulo");
        return !ahora.isBefore(fechaEntrada.atTime(hora));
    }
}

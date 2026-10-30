package co.edu.uniquindio.sga.domain.valueobject;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

/** L-12: horas de entrada y salida del alojamiento. */
public record HorarioAlojamiento(LocalTime horaEntrada, LocalTime horaSalida) {

    public HorarioAlojamiento {
        Objects.requireNonNull(horaEntrada, "La hora de entrada no puede ser nula");
        Objects.requireNonNull(horaSalida, "La hora de salida no puede ser nula");
    }

    /** 3.3: tiempo disponible, el mismo día, entre la salida de un grupo y la entrada del siguiente. */
    public Duration ventanaDeRecambio() {
        return Duration.between(horaSalida, horaEntrada);
    }

    /**
     * 3.3 / RN-20: si el tiempo de preparación excede la ventana, el apartamento no puede recibir una entrada el
     * mismo día de una salida. Si es igual a la ventana, alcanza justo.
     */
    public boolean permiteRecambioMismoDia(TiempoPreparacion tiempoPreparacion) {
        Objects.requireNonNull(tiempoPreparacion, "El tiempo de preparación no puede ser nulo");
        return !ventanaDeRecambio().minusHours(tiempoPreparacion.horas()).isNegative();
    }
}

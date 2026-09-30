package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Value Object que representa el rango de noches que ocupa una reserva.
 * <p>
 * 3.1 - El intervalo es cerrado en la entrada y abierto en la salida:
 * [entrada, salida). La noche de la fecha de salida no se cobra ni se ocupa.
 */
public record Estancia(LocalDate entrada, LocalDate salida) {

    public Estancia {
        Objects.requireNonNull(entrada, "La fecha de entrada no puede ser nula");
        Objects.requireNonNull(salida, "La fecha de salida no puede ser nula");
        // RN-03: la fecha de salida es posterior a la de entrada.
        if (!salida.isAfter(entrada)) {
            throw new ReglaDominioException(
                    "RN-03: la fecha de salida (" + salida + ") debe ser posterior a la fecha de entrada (" + entrada + ")");
        }
    }

    /**
     * RN-05 / 3.1: número de noches = días calendario entre entrada y salida.
     */
    public long noches() {
        return ChronoUnit.DAYS.between(entrada, salida);
    }

    /**
     * 3.1 - Dos estancias se solapan si comparten al menos una noche:
     * entrada_A < salida_B y entrada_B < salida_A.
     */
    public boolean seSolapaCon(Estancia otra) {
        Objects.requireNonNull(otra, "La estancia a comparar no puede ser nula");
        return this.entrada.isBefore(otra.salida) && otra.entrada.isBefore(this.salida);
    }

    /**
     * True si la fecha cae dentro del intervalo [entrada, salida).
     */
    public boolean incluyeFecha(LocalDate fecha) {
        Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        return !fecha.isBefore(entrada) && fecha.isBefore(salida);
    }

    /**
     * RN-04: no se pueden crear reservas cuya fecha de entrada sea anterior a hoy.
     * Recibe la fecha "actual" como parámetro (nunca LocalDate.now()) para que
     * el dominio sea testeable con fechas fijas.
     */
    public boolean entradaEsAnteriorA(LocalDate fechaReferencia) {
        Objects.requireNonNull(fechaReferencia, "La fecha de referencia no puede ser nula");
        return this.entrada.isBefore(fechaReferencia);
    }
}
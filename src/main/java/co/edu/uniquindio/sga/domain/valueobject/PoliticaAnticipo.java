package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * L-11: si se exige anticipo y de cuánto. Es un PORCENTAJE (0.5 = 50%) y no un monto fijo, porque el anticipo
 * depende del valor de cada estancia.
 */
public record PoliticaAnticipo(boolean exige, BigDecimal porcentaje) {

    public PoliticaAnticipo {
        Objects.requireNonNull(porcentaje, "El porcentaje de anticipo no puede ser nulo");
        if (exige) {
            if (porcentaje.compareTo(BigDecimal.ZERO) <= 0 || porcentaje.compareTo(BigDecimal.ONE) > 0) {
                throw new ReglaDominioException("El porcentaje de anticipo debe estar entre 0 (exclusivo) y 1");
            }
        } else {
            porcentaje = BigDecimal.ZERO; // sin anticipo no hay porcentaje: se normaliza
        }
    }

    public static PoliticaAnticipo noExige() {
        return new PoliticaAnticipo(false, BigDecimal.ZERO);
    }

    /** Anticipo a pagar al confirmar una reserva con el valor total dado. */
    public Dinero calcularAnticipo(Dinero valorTotal) {
        Objects.requireNonNull(valorTotal, "El valor total no puede ser nulo");
        return exige ? valorTotal.multiplicar(porcentaje) : Dinero.cero();
    }
}

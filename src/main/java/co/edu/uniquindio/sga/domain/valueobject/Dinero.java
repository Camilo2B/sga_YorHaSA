package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que representa un monto en pesos colombianos (COP).
 * <p>
 * 3.4 - Moneda única: peso colombiano (COP), sin decimales. Todo valor
 * monetario se representa con un tipo de precisión exacta (BigDecimal);
 * prohibido usar float o double. Redondeo al peso más cercano.
 * <p>
 * Un record ya provee equals/hashCode por valor y es inmutable por diseño
 * (no hay forma de mutar 'monto' una vez construido).
 */
public record Dinero(BigDecimal monto) {

    private static final String MONEDA = "COP";

    public Dinero {
        Objects.requireNonNull(monto, "El monto no puede ser nulo");
        // 3.4: redondeo al peso más cercano, sin decimales.
        monto = monto.setScale(0, RoundingMode.HALF_UP);
    }

    public static Dinero of(long monto) {
        return new Dinero(BigDecimal.valueOf(monto));
    }

    public static Dinero cero() {
        return new Dinero(BigDecimal.ZERO);
    }

    public Dinero sumar(Dinero otro) {
        Objects.requireNonNull(otro, "El dinero a sumar no puede ser nulo");
        return new Dinero(this.monto.add(otro.monto));
    }

    public Dinero restar(Dinero otro) {
        Objects.requireNonNull(otro, "El dinero a restar no puede ser nulo");
        return new Dinero(this.monto.subtract(otro.monto));
    }

    public Dinero multiplicar(int factor) {
        if (factor < 0) {
            throw new ReglaDominioException("No se puede multiplicar un Dinero por un factor negativo");
        }
        return new Dinero(this.monto.multiply(BigDecimal.valueOf(factor)));
    }

    /**
     * Multiplica por un porcentaje/factor decimal (ej. 0.5 para 50%).
     * Útil para calcular retenciones, descuentos y recargos.
     */
    public Dinero multiplicar(BigDecimal factor) {
        Objects.requireNonNull(factor, "El factor no puede ser nulo");
        return new Dinero(this.monto.multiply(factor));
    }

    public boolean esMayorQue(Dinero otro) {
        Objects.requireNonNull(otro, "El dinero a comparar no puede ser nulo");
        return this.monto.compareTo(otro.monto) > 0;
    }

    public boolean esCero() {
        return this.monto.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean esNegativo() {
        return this.monto.compareTo(BigDecimal.ZERO) < 0;
    }

    public String moneda() {
        return MONEDA;
    }
}
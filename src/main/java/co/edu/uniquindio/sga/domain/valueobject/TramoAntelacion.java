package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 7.4 - Un tramo de antelación define, para una cancelación con al menos
 * cierta cantidad de horas de anticipación, qué porcentaje del anticipo se
 * retiene. La devolución NUNCA se guarda aparte: siempre es el complemento
 * de la retención (100% - retención), el mismo principio que aplicamos para
 * no duplicar valorCongelado en Reserva.
 */
public record TramoAntelacion(int antelacionMinimaHoras, BigDecimal porcentajeRetencion) {

    public TramoAntelacion {
        if (antelacionMinimaHoras < 0) {
            throw new ReglaDominioException("La antelación mínima no puede ser negativa");
        }
        Objects.requireNonNull(porcentajeRetencion, "El porcentaje de retención no puede ser nulo");
        if (porcentajeRetencion.compareTo(BigDecimal.ZERO) < 0 || porcentajeRetencion.compareTo(BigDecimal.ONE) > 0) {
            throw new ReglaDominioException("El porcentaje de retención debe estar entre 0 y 1");
        }
    }

    public BigDecimal porcentajeDevolucion() {
        return BigDecimal.ONE.subtract(porcentajeRetencion);
    }

    /**
     * True si una cancelación con esta cantidad de horas de antelación cae
     * dentro de este tramo (antelación real >= mínimo exigido por el tramo).
     */
    public boolean aplicaPara(long horasDeAntelacion) {
        return horasDeAntelacion >= antelacionMinimaHoras;
    }
}
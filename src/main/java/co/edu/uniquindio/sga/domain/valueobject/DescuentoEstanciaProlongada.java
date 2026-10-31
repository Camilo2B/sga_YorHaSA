package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * RN-Propia-01 (Descuento por Órbita Prolongada): una estancia de {@code nochesMinimas} o más noches continuas, en
 * cualquier temporada, recibe un porcentaje de descuento sobre la tarifa de cada noche. Ambos valores son parámetros
 * del alojamiento (application.properties), nunca constantes del código.
 */
public record DescuentoEstanciaProlongada(int nochesMinimas, BigDecimal porcentaje) {

    public DescuentoEstanciaProlongada {
        if (nochesMinimas < 1) {
            throw new ReglaDominioException("Las noches mínimas del descuento por estancia prolongada deben ser al menos 1");
        }
        Objects.requireNonNull(porcentaje, "El porcentaje de descuento no puede ser nulo");
        if (porcentaje.compareTo(BigDecimal.ZERO) < 0 || porcentaje.compareTo(BigDecimal.ONE) >= 0) {
            throw new ReglaDominioException("El porcentaje de descuento debe ser mayor o igual a 0 y menor que 1");
        }
    }

    public boolean aplicaPara(long noches) {
        return noches >= nochesMinimas;
    }

    /** Tarifa con el descuento aplicado, redondeada al peso (3.4). */
    public Dinero aplicarA(Dinero tarifa) {
        Objects.requireNonNull(tarifa, "La tarifa no puede ser nula");
        return tarifa.multiplicar(BigDecimal.ONE.subtract(porcentaje));
    }
}

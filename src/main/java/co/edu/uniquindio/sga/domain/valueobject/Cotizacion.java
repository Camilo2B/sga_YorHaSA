package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.List;
import java.util.Objects;

/**
 * 7.4 - Calcula y congela el valor de una estancia con su desglose noche por
 * noche.
 * <p>
 * RN-22: el valor de una reserva queda congelado al crearla; Reserva
 * compone una Cotizacion (no un número suelto tipo "valorCongelado"), así
 * que no hay dos lugares donde ese dato pueda desincronizarse.
 */
public record Cotizacion(List<DetalleNoche> desglosePorNoche) {

    public Cotizacion {
        Objects.requireNonNull(desglosePorNoche, "El desglose no puede ser nulo");
        if (desglosePorNoche.isEmpty()) {
            throw new ReglaDominioException("Una cotización debe tener al menos una noche en su desglose");
        }
        desglosePorNoche = List.copyOf(desglosePorNoche); // inmutable, sin aliasing con la lista original
    }

    /**
     * RN-05: el valor de la estancia es la suma, noche por noche, del
     * subtotal de cada DetalleNoche (tarifa vigente x ocupantes facturables).
     */
    public Dinero valorTotal() {
        return desglosePorNoche.stream()
                .map(DetalleNoche::subtotal)
                .reduce(Dinero.cero(), Dinero::sumar);
    }

    public int totalNoches() {
        return desglosePorNoche.size();
    }
}
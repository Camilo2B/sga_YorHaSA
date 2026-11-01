package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.regex.Pattern;

/**
 * Identificador de negocio de una reserva, con el formato RES-AAAA-NNNNN (por ejemplo RES-2026-00001): año de
 * creación y consecutivo dentro del año. Es el que ve el huésped y el que usa la API; el UUID de la entidad queda
 * como detalle interno.
 */
public record CodigoReserva(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^RES-\\d{4}-\\d{5,}$");

    public CodigoReserva {
        if (valor == null || !FORMATO.matcher(valor).matches()) {
            throw new ReglaDominioException("El código de reserva debe tener el formato RES-AAAA-NNNNN");
        }
    }

    public static CodigoReserva de(int anio, long consecutivo) {
        return new CodigoReserva(String.format("RES-%04d-%05d", anio, consecutivo));
    }
}

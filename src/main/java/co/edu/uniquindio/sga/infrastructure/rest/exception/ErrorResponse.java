package co.edu.uniquindio.sga.infrastructure.rest.exception;

import java.util.List;

/**
 * Único formato de error de la API. Nunca lleva trazas técnicas: solo un código estable,
 * un mensaje con significado de negocio y, si aplica, el detalle por campo.
 */
public record ErrorResponse(String codigoError, String mensaje, List<String> detalles) {

    public static ErrorResponse de(String codigoError, String mensaje) {
        return new ErrorResponse(codigoError, mensaje, List.of());
    }
}

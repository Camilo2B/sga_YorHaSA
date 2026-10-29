package co.edu.uniquindio.sga.application.exception;

/**
 * Recurso inexistente: pertenece a la capa de aplicación (no es una regla de negocio violada),
 * y el @RestControllerAdvice la traduce a 404.
 */
public class ApartamentoNoEncontradoException extends RuntimeException {

    public ApartamentoNoEncontradoException(String identificacion) {
        super("No se encontró el apartamento con identificación: " + identificacion);
    }
}

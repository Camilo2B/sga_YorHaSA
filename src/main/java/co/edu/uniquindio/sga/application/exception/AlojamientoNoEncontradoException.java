package co.edu.uniquindio.sga.application.exception;

/** El sistema administra un único alojamiento (F-13); si todavía no se ha cargado, la consulta no encuentra nada. */
public class AlojamientoNoEncontradoException extends RuntimeException {

    public AlojamientoNoEncontradoException() {
        super("El alojamiento todavía no ha sido configurado");
    }
}

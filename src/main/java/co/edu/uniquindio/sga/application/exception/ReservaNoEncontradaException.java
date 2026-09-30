package co.edu.uniquindio.sga.application.exception;

import java.util.UUID;

public class ReservaNoEncontradaException extends RuntimeException {

    public ReservaNoEncontradaException(UUID id) {
        super("No se encontró la reserva con ID: " + id);
    }

    public ReservaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
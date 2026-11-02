package co.edu.uniquindio.sga.application.exception;

import java.util.UUID;

public class BloqueoNoEncontradoException extends RuntimeException {

    public BloqueoNoEncontradoException(String identificacionApartamento, UUID id) {
        super("No se encontró el bloqueo " + id + " del apartamento " + identificacionApartamento);
    }
}

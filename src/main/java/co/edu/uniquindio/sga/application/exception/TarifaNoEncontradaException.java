package co.edu.uniquindio.sga.application.exception;

import java.util.UUID;

public class TarifaNoEncontradaException extends RuntimeException {

    public TarifaNoEncontradaException(String identificacionApartamento, UUID id) {
        super("No se encontró la tarifa " + id + " del apartamento " + identificacionApartamento);
    }
}

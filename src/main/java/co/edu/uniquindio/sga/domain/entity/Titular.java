package co.edu.uniquindio.sga.domain.entity;

import java.time.LocalDate;

/**
 * 3.6 - Responsable de la reserva y de su pago. Titular y ocupante son
 * conceptos del negocio: existen aunque nunca usen el sistema (un titular
 * puede no tener usuario asociado).
 * <p>
 * Hereda de Ocupante porque un titular ES un ocupante con una
 * responsabilidad adicional: 3.2 exige que "el titular sea siempre un
 * ocupante facturable de la reserva". Esa condición NO se valida en este
 * constructor a propósito, se valida en Reserva.crear(), que sí tiene ambos
 * disponibles en el momento de decidir si ese titular puede serlo para esa
 * estancia en particular.
 */
public final class Titular extends Ocupante {

    private Titular(String documento, String nombre, LocalDate fechaNacimiento) {
        super(documento, nombre, fechaNacimiento);
    }

    public static Titular crear(String documento, String nombre, LocalDate fechaNacimiento) {
        return new Titular(documento, nombre, fechaNacimiento);
    }
}
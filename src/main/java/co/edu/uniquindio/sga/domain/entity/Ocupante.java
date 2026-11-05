package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Persona incluida en una reserva, con su fecha de nacimiento (nunca
 * su edad: la edad se calcula, nunca se almacena).
 * Entidad: dos ocupantes con los mismos datos son distintos; interesa su
 * historia (puede aparecer en varias reservas a lo largo del tiempo).
 */
public class Ocupante {

    private final DocumentoIdentidad documento;
    private final String nombre;
    private final LocalDate fechaNacimiento;

    Ocupante(DocumentoIdentidad documento, String nombre, LocalDate fechaNacimiento) {
        Objects.requireNonNull(documento, "El documento de identidad no puede ser nulo");
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El nombre del ocupante no puede estar vacío");
        }
        Objects.requireNonNull(fechaNacimiento, "La fecha de nacimiento no puede ser nula");
        this.documento = documento;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
    }

    public static Ocupante crear(String numeroDocumento, String nombre, LocalDate fechaNacimiento) {
        return new Ocupante(new DocumentoIdentidad(numeroDocumento), nombre, fechaNacimiento);
    }

    /**
     * Edad en años cumplidos a una fecha de referencia. Recibe la fecha
     * como parámetro (nunca LocalDate.now()) para que las pruebas puedan
     * usar fechas fijas.
     */
    public int calcularEdad(LocalDate fechaReferencia) {
        Objects.requireNonNull(fechaReferencia, "La fecha de referencia no puede ser nula");
        if (fechaReferencia.isBefore(fechaNacimiento)) {
            throw new ReglaDominioException("La fecha de referencia no puede ser anterior a la fecha de nacimiento");
        }
        return Period.between(fechaNacimiento, fechaReferencia).getYears();
    }

    /**
     * RN-06: un ocupante es facturable si, a la fecha de entrada de la
     * estancia, su edad alcanza o supera el umbral configurado del
     * alojamiento.
     */
    public boolean esFacturable(LocalDate fechaEntrada, int umbralEdadFacturable) {
        return calcularEdad(fechaEntrada) >= umbralEdadFacturable;
    }

    public DocumentoIdentidad documento() {
        return documento;
    }

    public String nombre() {
        return nombre;
    }

    public LocalDate fechaNacimiento() {
        return fechaNacimiento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ocupante otro)) return false;
        return this.documento.equals(otro.documento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documento);
    }
}
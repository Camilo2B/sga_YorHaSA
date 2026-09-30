package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * 3.2 - Persona incluida en una reserva, con su fecha de nacimiento (nunca
 * su edad: la edad se calcula, nunca se almacena).
 * <p>
 * Entidad: dos ocupantes con los mismos datos son distintos; interesa su
 * historia (puede aparecer en varias reservas a lo largo del tiempo).
 * <p>
 * El constructor es de paquete (sin modificador), no estrictamente privado,
 * porque Titular extiende esta clase y necesita invocar super(...). Ninguna
 * clase fuera de domain.entity puede construir un Ocupante de todas formas
 * -ni siquiera con reflexión "normal" desde application/infrastructure-,
 * así que la intención de la rúbrica ("sin construcción externa arbitraria")
 * se mantiene igual.
 */
public class Ocupante {

    private final String documento;
    private final String nombre;
    private final LocalDate fechaNacimiento;

    Ocupante(String documento, String nombre, LocalDate fechaNacimiento) {
        if (documento == null || documento.isBlank()) {
            throw new ReglaDominioException("El documento del ocupante no puede estar vacío");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El nombre del ocupante no puede estar vacío");
        }
        Objects.requireNonNull(fechaNacimiento, "La fecha de nacimiento no puede ser nula");
        this.documento = documento;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
    }

    public static Ocupante crear(String documento, String nombre, LocalDate fechaNacimiento) {
        return new Ocupante(documento, nombre, fechaNacimiento);
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
     * alojamiento. Un ocupante que cumple años durante la estancia no
     * cambia de condición a mitad de camino -por eso se evalúa siempre
     * contra la fecha de ENTRADA, nunca la de salida ni la actual-.
     */
    public boolean esFacturable(LocalDate fechaEntrada, int umbralEdadFacturable) {
        return calcularEdad(fechaEntrada) >= umbralEdadFacturable;
    }

    public String documento() {
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
package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Gravedad;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * 7.6 - Reporte de un daño, faltante o situación en un apartamento: fecha,
 * autor, descripción y gravedad.
 * <p>
 * Entidad: cada reporte es un hecho distinto aunque dos coincidan en texto;
 * interesa conservar el historial completo de novedades de un apartamento.
 * <p>
 * Totalmente inmutable para el Corte 1: 7.6 solo pide "registrar" y
 * "consultar", nada de reclasificar. Cuando llegue el Corte 4 (IA no
 * bloqueante, A.6), ahí sí tendrá sentido agregar un método de dominio
 * explícito como reclasificar(Gravedad nueva, String origen) -nunca un
 * setGravedad() genérico-; no se adelanta ahora porque no hay ninguna regla
 * de este corte que lo exija todavía.
 */
public final class Novedad {

    private final UUID id;
    private final LocalDate fecha;
    private final String autor;
    private final String descripcion;
    private final Gravedad gravedad;

    private Novedad(UUID id, LocalDate fecha, String autor, String descripcion, Gravedad gravedad) {
        this.id = id;
        this.fecha = fecha;
        this.autor = autor;
        this.descripcion = descripcion;
        this.gravedad = gravedad;
    }

    public static Novedad crear(LocalDate fecha, String autor, String descripcion, Gravedad gravedad) {
        Objects.requireNonNull(fecha, "La fecha de la novedad no puede ser nula");
        if (autor == null || autor.isBlank()) {
            throw new ReglaDominioException("El autor de la novedad no puede estar vacío");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaDominioException("La descripción de la novedad no puede estar vacía");
        }
        Objects.requireNonNull(gravedad, "La gravedad de la novedad no puede ser nula");
        return new Novedad(UUID.randomUUID(), fecha, autor, descripcion, gravedad);
    }

    public UUID id() {
        return id;
    }

    public LocalDate fecha() {
        return fecha;
    }

    public String autor() {
        return autor;
    }

    public String descripcion() {
        return descripcion;
    }

    public Gravedad gravedad() {
        return gravedad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Novedad otra)) return false;
        return this.id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
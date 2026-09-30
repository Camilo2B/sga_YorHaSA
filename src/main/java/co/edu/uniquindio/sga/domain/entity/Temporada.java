package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * 4 - Periodo del calendario con tarifas propias.
 * 7.4 - Las temporadas no pueden solaparse entre sí. Existe una temporada
 * base obligatoria que cubre todas las fechas no asignadas a otra temporada.
 * <p>
 * Entidad: tiene identidad propia; interesa distinguir cada temporada aunque
 * dos pudieran compartir nombre o fechas en teoría.
 */
public final class Temporada {

    private final UUID id;
    private final String nombre;
    private final LocalDate fechaInicio; // null si esBase
    private final LocalDate fechaFin;    // null si esBase
    private final boolean esBase;

    private Temporada(UUID id, String nombre, LocalDate fechaInicio, LocalDate fechaFin, boolean esBase) {
        this.id = id;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.esBase = esBase;
    }

    public static Temporada crear(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        validarNombre(nombre);
        Objects.requireNonNull(fechaInicio, "La fecha de inicio no puede ser nula");
        Objects.requireNonNull(fechaFin, "La fecha de fin no puede ser nula");
        if (!fechaFin.isAfter(fechaInicio)) {
            throw new ReglaDominioException("La fecha de fin de la temporada debe ser posterior a la fecha de inicio");
        }
        return new Temporada(UUID.randomUUID(), nombre, fechaInicio, fechaFin, false);
    }

    /**
     * 7.4: la temporada base cubre todas las fechas no asignadas a otra
     * temporada; por eso no tiene rango propio de fechas.
     */
    public static Temporada crearBase(String nombre) {
        validarNombre(nombre);
        return new Temporada(UUID.randomUUID(), nombre, null, null, true);
    }

    private static void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El nombre de la temporada no puede estar vacío");
        }
    }

    /**
     * True si la fecha cae dentro de esta temporada. La base siempre
     * responde true (es el respaldo); resolver cuál prevalece cuando varias
     * responden true es responsabilidad de Alojamiento, no de Temporada.
     */
    public boolean cubre(LocalDate fecha) {
        Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        if (esBase) {
            return true;
        }
        return !fecha.isBefore(fechaInicio) && fecha.isBefore(fechaFin);
    }

    /**
     * 7.4: las temporadas no pueden solaparse entre sí. La base queda fuera
     * de esta validación: por definición no compite por fechas, es el
     * respaldo cuando ninguna otra aplica.
     */
    public boolean seSolapaCon(Temporada otra) {
        Objects.requireNonNull(otra, "La temporada a comparar no puede ser nula");
        if (this.esBase || otra.esBase) {
            return false;
        }
        return this.fechaInicio.isBefore(otra.fechaFin) && otra.fechaInicio.isBefore(this.fechaFin);
    }

    public UUID id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public LocalDate fechaInicio() {
        return fechaInicio;
    }

    public LocalDate fechaFin() {
        return fechaFin;
    }

    public boolean esBase() {
        return esBase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Temporada otra)) return false;
        return this.id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
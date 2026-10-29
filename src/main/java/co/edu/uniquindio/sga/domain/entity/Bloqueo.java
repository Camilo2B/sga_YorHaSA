package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;

import java.util.Objects;
import java.util.UUID;

/**
 * 3.3 / 4 - Registro administrativo, con rango de fechas y motivo, que
 * impide vender un apartamento durante ese rango (RN-07).
 * <p>
 * Vive DENTRO del agregado Apartamento (no tiene repositorio propio: se
 * guarda y se recupera a través de ApartamentoRepository).
 * <p>
 * El rango se modela con Estancia, es decir, semiabierto [inicio, fin): la
 * misma regla de noches que usan las reservas (3.1). Así disponibilidad
 * compara bloqueos y reservas con UN solo criterio de solapamiento, y en
 * base de datos ambos se consultan con el mismo predicado
 * (inicio &lt; :fin AND fin &gt; :inicio) y se mapean con el mismo @Embeddable.
 */
public final class Bloqueo {

    private final UUID id;
    private final Estancia rango;
    private final String motivo;

    private Bloqueo(UUID id, Estancia rango, String motivo) {
        this.id = id;
        this.rango = rango;
        this.motivo = motivo;
    }

    public static Bloqueo crear(Estancia rango, String motivo) {
        Objects.requireNonNull(rango, "El rango de fechas del bloqueo no puede ser nulo");
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("El motivo del bloqueo no puede estar vacío");
        }
        return new Bloqueo(UUID.randomUUID(), rango, motivo.trim());
    }

    /** Reconstruye un Bloqueo ya persistido (conserva su id). Solo para rehidratar desde la persistencia. */
    public static Bloqueo reconstruir(UUID id, Estancia rango, String motivo) {
        return new Bloqueo(id, rango, motivo);
    }

    /** RN-07: un apartamento con bloqueo vigente sobre una noche no está disponible para esa noche. */
    public boolean seSolapaCon(Estancia estanciaConsultada) {
        Objects.requireNonNull(estanciaConsultada, "La estancia a consultar no puede ser nula");
        return this.rango.seSolapaCon(estanciaConsultada);
    }

    public UUID id() {
        return id;
    }

    public Estancia rango() {
        return rango;
    }

    public String motivo() {
        return motivo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bloqueo otro)) return false;
        return this.id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

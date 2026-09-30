package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;

import java.util.Objects;
import java.util.UUID;

/**
 * 3.3 / 4 - Registro administrativo, con rango de fechas y motivo, que
 * impide vender un apartamento durante ese rango (RN-07).
 * <p>
 * Reutiliza Estancia como VO para el rango: un bloqueo es, en la forma,
 * exactamente lo mismo que una estancia (un intervalo [inicio, fin) con la
 * misma regla de solapamiento de 3.1) — no hay razón de negocio para
 * duplicar esa validación en una clase de rango de fechas aparte.
 * <p>
 * Entidad: dos bloqueos con las mismas fechas y motivo son distintos
 * (episodios administrativos separados); interesa su historia.
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
        return new Bloqueo(UUID.randomUUID(), rango, motivo);
    }

    /**
     * RN-07: un apartamento con bloqueo vigente sobre una noche no está
     * disponible para esa noche.
     */
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
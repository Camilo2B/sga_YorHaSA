package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;

import java.util.Objects;
import java.util.UUID;

/**
 * 7.7 - Concepto que suma al folio: alojamiento, servicio adicional, ajuste
 * por modificación o penalidad.
 * <p>
 * RN-16: los cargos no se modifican ni se eliminan; toda corrección es un
 * movimiento inverso. Por diseño, Cargo es completamente inmutable -no hay
 * forma de tocar sus campos una vez creado-, así que esa regla no depende
 * de la disciplina de quien lo use: es imposible editarlo.
 * <p>
 * A propósito NO se restringe el signo de "valor": una corrección se
 * registra como un Cargo nuevo con valor negativo (ver Folio, que es quien
 * decide cuándo un cargo es "el original" y cuándo es "la corrección" -
 * Cargo, por sí solo, no conoce esa distinción-).
 */
public final class Cargo {

    private final UUID id;
    private final TipoCargo tipo;
    private final Dinero valor;

    private Cargo(UUID id, TipoCargo tipo, Dinero valor) {
        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
    }

    public static Cargo crear(TipoCargo tipo, Dinero valor) {
        Objects.requireNonNull(tipo, "El tipo de cargo no puede ser nulo");
        Objects.requireNonNull(valor, "El valor del cargo no puede ser nulo");
        return new Cargo(UUID.randomUUID(), tipo, valor);
    }

    public UUID id() {
        return id;
    }

    public TipoCargo tipo() {
        return tipo;
    }

    public Dinero valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cargo otro)) return false;
        return this.id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
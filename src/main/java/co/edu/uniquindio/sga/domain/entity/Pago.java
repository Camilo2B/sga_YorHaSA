package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * 4 / 7.7 - Abono registrado contra un folio, con medio y fecha.
 * <p>
 * RN-16: los pagos no se modifican ni se eliminan; toda corrección es un
 * movimiento inverso. Inmutable por diseño, igual que Cargo.
 * <p>
 * A diferencia de Cargo, aquí SÍ se exige valor estrictamente positivo: un
 * pago siempre es dinero real entrando. Una devolución (3.4: "el sistema
 * registra movimientos de dinero; no los ejecuta") no es un Pago negativo,
 * se registra como un Cargo negativo -Pago representa específicamente la
 * entrada de dinero del huésped, no cualquier movimiento del folio-.
 */
public final class Pago {

    private final UUID id;
    private final MedioPago medio;
    private final LocalDate fecha;
    private final Dinero valor;

    private Pago(UUID id, MedioPago medio, LocalDate fecha, Dinero valor) {
        this.id = id;
        this.medio = medio;
        this.fecha = fecha;
        this.valor = valor;
    }

    public static Pago crear(MedioPago medio, LocalDate fecha, Dinero valor) {
        Objects.requireNonNull(medio, "El medio de pago no puede ser nulo");
        Objects.requireNonNull(fecha, "La fecha del pago no puede ser nula");
        Objects.requireNonNull(valor, "El valor del pago no puede ser nulo");
        if (valor.esNegativo() || valor.esCero()) {
            throw new ReglaDominioException("Un pago debe tener un valor positivo");
        }
        return new Pago(UUID.randomUUID(), medio, fecha, valor);
    }

    public UUID id() {
        return id;
    }

    public MedioPago medio() {
        return medio;
    }

    public LocalDate fecha() {
        return fecha;
    }

    public Dinero valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pago otro)) return false;
        return this.id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.entity.Cargo;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa la cuenta contable o estado de cuenta asociado a una reserva.
 * Registra todos los cargos aplicados (hospedaje, penalizaciones, servicios extra)
 * y los pagos realizados por el huésped para calcular el saldo pendiente.
 */
public class Folio {

    private final UUID id;
    private final UUID reservaId;
    private final List<Cargo> cargos;
    private final List<Pago> pagos;

    private Folio(UUID id, UUID reservaId, List<Cargo> cargos, List<Pago> pagos) {
        this.id = Objects.requireNonNull(id, "El ID del folio no puede ser nulo.");
        this.reservaId = Objects.requireNonNull(reservaId, "El ID de la reserva no puede ser nulo.");
        this.cargos = cargos != null ? new ArrayList<>(cargos) : new ArrayList<>();
        this.pagos = pagos != null ? new ArrayList<>(pagos) : new ArrayList<>();
    }

    /** Abre el folio vacío de una reserva nueva. */
    public static Folio crear(UUID reservaId) {
        return new Folio(UUID.randomUUID(), reservaId, List.of(), List.of());
    }

    /** Reconstruye un folio ya persistido con sus cargos y pagos. Solo para rehidratar desde la persistencia. */
    public static Folio reconstruir(UUID id, UUID reservaId, List<Cargo> cargos, List<Pago> pagos) {
        return new Folio(id, reservaId, cargos, pagos);
    }

    /**
     * Registra un nuevo cargo en la cuenta contable del folio.
     */
    public void agregarCargo(Cargo cargo) {
        Objects.requireNonNull(cargo, "El cargo a agregar no puede ser nulo.");
        this.cargos.add(cargo);
    }

    /**
     * Registra un pago recibido y lo asocia al folio.
     */
    /** Igual que el siguiente, con la fecha de hoy según el reloj del sistema. Prefiera la versión con fecha explícita. */
    public Pago registrarPago(Dinero monto, MedioPago medioPago, String referencia) {
        return registrarPago(monto, medioPago, referencia, LocalDate.now());
    }

    public Pago registrarPago(Dinero monto, MedioPago medioPago, String referencia, LocalDate fecha) {
        Objects.requireNonNull(monto, "El monto del pago no puede ser nulo.");
        Objects.requireNonNull(medioPago, "El medio de pago no puede ser nulo.");

        if (monto.esNegativo() || monto.esCero()) {
            throw new ReglaDominioException("El monto del pago debe ser mayor a cero.");
        }

        // RN-15: el saldo es cargos menos pagos y nunca queda negativo (Dinero no admite negativos): no se paga de más.
        Dinero saldoPendiente = calcularSaldoPendiente();
        if (monto.esMayorQue(saldoPendiente)) {
            throw new ReglaDominioException("El pago de " + monto.monto() + " supera el saldo pendiente del folio ("
                    + saldoPendiente.monto() + ").");
        }

        Pago nuevoPago = Pago.crear(medioPago, fecha, monto);
        this.pagos.add(nuevoPago);
        return nuevoPago;
    }

    /**
     * Calcula el total acumulado de todos los cargos asociados al folio.
     */
    public Dinero calcularTotalCargos() {
        return cargos.stream()
                .map(Cargo::valor)
                .reduce(Dinero.cero(), Dinero::sumar);
    }

    /**
     * Calcula el total acumulado de todos los pagos registrados.
     */
    public Dinero calcularTotalPagos() {
        return pagos.stream()
                .map(Pago::valor)
                .reduce(Dinero.cero(), Dinero::sumar);
    }

    /**
     * Calcula el saldo pendiente por saldar (Cargos - Pagos).
     */
    public Dinero calcularSaldoPendiente() {
        return calcularTotalCargos().restar(calcularTotalPagos());
    }

    /**
     * Verifica si la cuenta está completamente pagada (saldo igual o menor a cero).
     */
    public boolean estaPazYSalvo() {
        Dinero saldo = calcularSaldoPendiente();
        return saldo.esNegativo() || saldo.esCero();
    }

    public UUID getId() {
        return id;
    }

    public UUID getReservaId() {
        return reservaId;
    }

    public List<Cargo> getCargos() {
        return Collections.unmodifiableList(cargos);
    }

    public List<Pago> getPagos() {
        return Collections.unmodifiableList(pagos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Folio folio = (Folio) o;
        return Objects.equals(id, folio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
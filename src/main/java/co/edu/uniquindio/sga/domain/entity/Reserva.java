package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DetalleNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.Gravedad;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad principal del dominio que gestiona el ciclo de vida completo de una reserva de alojamiento,
 * sus ocupantes, novedades operativas, transiciones de estado y transacciones financieras.
 */
public class Reserva {

    private final UUID id;
    private final UUID apartamentoId;
    private final Titular titular;
    private final Estancia estancia;
    private final CanalOrigen canalOrigen;
    private EstadoReserva estado;
    private final Cotizacion cotizacion;
    private final Folio folio;
    private final List<Ocupante> ocupantes;
    private final List<Novedad> novedades;

    public Reserva(UUID apartamentoId, Titular titular, Estancia estancia, CanalOrigen canalOrigen, Cotizacion cotizacion) {
        this.id = UUID.randomUUID();
        this.apartamentoId = Objects.requireNonNull(apartamentoId, "El ID del apartamento no puede ser nulo.");
        this.titular = Objects.requireNonNull(titular, "El titular de la reserva no puede ser nulo.");
        this.estancia = Objects.requireNonNull(estancia, "La estancia no puede ser nula.");
        this.canalOrigen = Objects.requireNonNull(canalOrigen, "El canal de origen no puede ser nulo.");
        this.cotizacion = Objects.requireNonNull(cotizacion, "La cotización no puede ser nula.");

        this.estado = EstadoReserva.PENDIENTE;
        this.folio = new Folio(this.id);
        this.ocupantes = new ArrayList<>();
        this.novedades = new ArrayList<>();

        inicializarCargosHospedaje();
    }

    public Reserva(UUID id, UUID apartamentoId, Titular titular, Estancia estancia, CanalOrigen canalOrigen,
                   EstadoReserva estado, Cotizacion cotizacion, Folio folio, List<Ocupante> ocupantes, List<Novedad> novedades) {
        this.id = Objects.requireNonNull(id, "El ID de la reserva no puede ser nulo.");
        this.apartamentoId = Objects.requireNonNull(apartamentoId, "El ID del apartamento no puede ser nulo.");
        this.titular = Objects.requireNonNull(titular, "El titular no puede ser nulo.");
        this.estancia = Objects.requireNonNull(estancia, "La estancia no puede ser nula.");
        this.canalOrigen = Objects.requireNonNull(canalOrigen, "El canal de origen no puede ser nulo.");
        this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo.");
        this.cotizacion = Objects.requireNonNull(cotizacion, "La cotización no puede ser nula.");
        this.folio = Objects.requireNonNull(folio, "El folio no puede ser nulo.");
        this.ocupantes = ocupantes != null ? new ArrayList<>(ocupantes) : new ArrayList<>();
        this.novedades = novedades != null ? new ArrayList<>(novedades) : new ArrayList<>();
    }

    /**
     * Carga automáticamente las noches cotizadas como cargos iniciales dentro del Folio.
     */
    private void inicializarCargosHospedaje() {
        for (DetalleNoche detalle : cotizacion.desglosePorNoche()) {
            Cargo cargoNoche = Cargo.crear(TipoCargo.ALOJAMIENTO, detalle.subtotal());
            this.folio.agregarCargo(cargoNoche);
        }
    }

    /**
     * Confirma la reserva una vez que se cumple con la garantía financiera o pago inicial requerido.
     */
    public void confirmar() {
        if (this.estado != EstadoReserva.PENDIENTE) {
            throw new ReglaDominioException("Solo se pueden confirmar reservas en estado PENDIENTE.");
        }
        this.estado = EstadoReserva.CONFIRMADA;
    }

    /**
     * Registra la entrada formal de los huéspedes al apartamento (Check-in).
     */
    public void realizarCheckIn() {
        if (this.estado != EstadoReserva.CONFIRMADA) {
            throw new ReglaDominioException("Para realizar Check-in la reserva debe estar CONFIRMADA.");
        }
        if (this.ocupantes.isEmpty()) {
            throw new ReglaDominioException("Debe registrar al menos un ocupante para realizar el Check-in.");
        }
        this.estado = EstadoReserva.EN_CURSO;
    }

    /**
     * Registra la salida formal (Check-out), verificando previamente que la cuenta no tenga saldo pendiente.
     */
    public void realizarCheckOut() {
        if (this.estado != EstadoReserva.EN_CURSO) {
            throw new ReglaDominioException("Solo se puede realizar Check-out sobre reservas EN_CURSO.");
        }
        if (!this.folio.estaPazYSalvo()) {
            throw new ReglaDominioException("No se puede realizar Check-out. El folio registra un saldo pendiente de: "
                    + folio.calcularSaldoPendiente());
        }
        this.estado = EstadoReserva.FINALIZADA;
    }

    /**
     * Cancela la reserva aplicando la política correspondiente según la antelación.
     */
    public void cancelar(PoliticaCancelacion politica) {
        if (this.estado == EstadoReserva.FINALIZADA || this.estado == EstadoReserva.CANCELADA) {
            throw new ReglaDominioException("No se puede cancelar una reserva que ya está " + this.estado);
        }

        if (politica != null) {
            // 1. Calcular las horas de antelación hasta la fecha de entrada
            long horasAntelacion = java.time.temporal.ChronoUnit.HOURS.between(
                    java.time.LocalDateTime.now(),
                    this.estancia.entrada().atStartOfDay()
            );

            // 2. Obtener el porcentaje de retención y calcular el valor del cargo por penalidad
            BigDecimal porcentaje = politica.retencionAplicable(Math.max(0, horasAntelacion));
            Dinero penalizacion = this.cotizacion.valorTotal().multiplicar(porcentaje);

            // 3. Si la penalización es mayor a cero, registrar el cargo
            if (penalizacion.esMayorQue(Dinero.cero())) {
                Cargo cargoPenalizacion = Cargo.crear(TipoCargo.PENALIDAD, penalizacion);
                this.folio.agregarCargo(cargoPenalizacion);
            }
        }

        this.estado = EstadoReserva.CANCELADA;
    }

    /**
     * Registra a un nuevo acompañante/ocupante dentro del hospedaje.
     */
    public void agregarOcupante(Ocupante ocupante) {
        Objects.requireNonNull(ocupante, "El ocupante no puede ser nulo.");
        this.ocupantes.add(ocupante);
    }

    /**
     * Registra una novedad o caso operativo ocurrido durante la estancia.
     */
    public void registrarNovedad(String autor, String descripcion, Gravedad gravedad) {
        Novedad novedad = Novedad.crear(LocalDate.now(), autor, descripcion, gravedad);
        this.novedades.add(novedad);
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public UUID getApartamentoId() {
        return apartamentoId;
    }

    public Titular getTitular() {
        return titular;
    }

    public Estancia getEstancia() {
        return estancia;
    }

    public CanalOrigen getCanalOrigen() {
        return canalOrigen;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public Cotizacion getCotizacion() {
        return cotizacion;
    }

    public Folio getFolio() {
        return folio;
    }

    public List<Ocupante> getOcupantes() {
        return Collections.unmodifiableList(ocupantes);
    }

    public List<Novedad> getNovedades() {
        return Collections.unmodifiableList(novedades);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reserva reserva = (Reserva) o;
        return Objects.equals(id, reserva.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DetalleNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.Gravedad;
import co.edu.uniquindio.sga.domain.valueobject.RecargoLlegadaNocturna;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final CodigoReserva codigo;
    private final String apartamentoId;
    private final Titular titular;
    private final Estancia estancia;
    private final CanalOrigen canalOrigen;
    private EstadoReserva estado;
    private final Cotizacion cotizacion;
    private final VersionPolitica versionPolitica;
    private final LocalDateTime fechaCreacion;
    private final boolean conMascota;
    private LocalTime horaEstimadaLlegada;
    private final Folio folio;
    private final List<Ocupante> ocupantes;
    private final List<Novedad> novedades;

    /**
     * Crea una reserva nueva en estado PENDIENTE. La cotización (RN-22) y la versión de la política de cancelación
     * quedan congeladas: un cambio posterior de tarifas o de política no altera esta reserva. fechaCreacion es la
     * base del plazo de confirmación (RN-21); la entrega quien invoca, nunca el dominio.
     */
    public Reserva(CodigoReserva codigo, String apartamentoId, Titular titular, Estancia estancia, CanalOrigen canalOrigen,
                   Cotizacion cotizacion, VersionPolitica versionPolitica, LocalDateTime fechaCreacion) {
        this(codigo, apartamentoId, titular, estancia, canalOrigen, cotizacion, versionPolitica, fechaCreacion, false);
    }

    /** Igual que el anterior, indicando si la reserva incluye mascota (RN-Propia-03: ocupa un cupo del apartamento). */
    public Reserva(CodigoReserva codigo, String apartamentoId, Titular titular, Estancia estancia, CanalOrigen canalOrigen,
                   Cotizacion cotizacion, VersionPolitica versionPolitica, LocalDateTime fechaCreacion, boolean conMascota) {
        this.id = UUID.randomUUID();
        this.codigo = Objects.requireNonNull(codigo, "El código de la reserva no puede ser nulo.");
        this.apartamentoId = Objects.requireNonNull(apartamentoId, "El ID del apartamento no puede ser nulo.");
        this.titular = Objects.requireNonNull(titular, "El titular de la reserva no puede ser nulo.");
        this.estancia = Objects.requireNonNull(estancia, "La estancia no puede ser nula.");
        this.canalOrigen = Objects.requireNonNull(canalOrigen, "El canal de origen no puede ser nulo.");
        this.cotizacion = Objects.requireNonNull(cotizacion, "La cotización no puede ser nula.");
        this.versionPolitica = Objects.requireNonNull(versionPolitica, "La versión de la política no puede ser nula.");
        this.fechaCreacion = Objects.requireNonNull(fechaCreacion, "La fecha de creación no puede ser nula.");
        this.conMascota = conMascota;
        this.horaEstimadaLlegada = null;

        this.estado = EstadoReserva.PENDIENTE;
        this.folio = new Folio(this.id);
        this.ocupantes = new ArrayList<>();
        this.novedades = new ArrayList<>();

        inicializarCargosHospedaje();
    }

    private Reserva(UUID id, CodigoReserva codigo, String apartamentoId, Titular titular, Estancia estancia,
                    CanalOrigen canalOrigen, EstadoReserva estado, Cotizacion cotizacion, VersionPolitica versionPolitica,
                    LocalDateTime fechaCreacion, boolean conMascota, LocalTime horaEstimadaLlegada, Folio folio,
                    List<Ocupante> ocupantes, List<Novedad> novedades) {
        this.id = Objects.requireNonNull(id, "El ID de la reserva no puede ser nulo.");
        this.codigo = Objects.requireNonNull(codigo, "El código de la reserva no puede ser nulo.");
        this.apartamentoId = Objects.requireNonNull(apartamentoId, "El ID del apartamento no puede ser nulo.");
        this.titular = Objects.requireNonNull(titular, "El titular no puede ser nulo.");
        this.estancia = Objects.requireNonNull(estancia, "La estancia no puede ser nula.");
        this.canalOrigen = Objects.requireNonNull(canalOrigen, "El canal de origen no puede ser nulo.");
        this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo.");
        this.cotizacion = Objects.requireNonNull(cotizacion, "La cotización no puede ser nula.");
        this.versionPolitica = Objects.requireNonNull(versionPolitica, "La versión de la política no puede ser nula.");
        this.fechaCreacion = Objects.requireNonNull(fechaCreacion, "La fecha de creación no puede ser nula.");
        this.conMascota = conMascota;
        this.horaEstimadaLlegada = horaEstimadaLlegada;
        this.folio = Objects.requireNonNull(folio, "El folio no puede ser nulo.");
        this.ocupantes = ocupantes != null ? new ArrayList<>(ocupantes) : new ArrayList<>();
        this.novedades = novedades != null ? new ArrayList<>(novedades) : new ArrayList<>();
    }

    /**
     * Reconstruye una reserva ya existente (la usa la persistencia): conserva el estado y el folio tal como estaban,
     * sin volver a generar los cargos de hospedaje.
     */
    public static Reserva reconstruir(UUID id, CodigoReserva codigo, String apartamentoId, Titular titular, Estancia estancia,
                                      CanalOrigen canalOrigen, EstadoReserva estado, Cotizacion cotizacion,
                                      VersionPolitica versionPolitica, LocalDateTime fechaCreacion, boolean conMascota,
                                      LocalTime horaEstimadaLlegada, Folio folio, List<Ocupante> ocupantes,
                                      List<Novedad> novedades) {
        return new Reserva(id, codigo, apartamentoId, titular, estancia, canalOrigen, estado, cotizacion,
                versionPolitica, fechaCreacion, conMascota, horaEstimadaLlegada, folio, ocupantes, novedades);
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
     * Registra la hora estimada de llegada (RN-09). Solo se registra una vez y antes de la entrada, porque el cargo
     * por llegada nocturna (RN-Propia-02) que puede generar no se puede deshacer: los cargos del folio no se modifican
     * ni se eliminan (RN-16).
     */
    public void registrarHoraEstimadaLlegada(LocalTime hora, RecargoLlegadaNocturna recargoNocturno) {
        Objects.requireNonNull(hora, "La hora estimada de llegada no puede ser nula.");
        Objects.requireNonNull(recargoNocturno, "El recargo por llegada nocturna no puede ser nulo.");
        if (this.estado != EstadoReserva.PENDIENTE && this.estado != EstadoReserva.CONFIRMADA) {
            throw new ReglaDominioException("La hora estimada de llegada solo se registra en reservas PENDIENTE o CONFIRMADA.");
        }
        if (this.horaEstimadaLlegada != null) {
            throw new ReglaDominioException("La hora estimada de llegada ya fue registrada para esta reserva.");
        }
        this.horaEstimadaLlegada = hora;
        if (recargoNocturno.aplicaA(hora)) {
            this.folio.agregarCargo(Cargo.crear(TipoCargo.SERVICIO_ADICIONAL, recargoNocturno.valor()));
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

    public CodigoReserva getCodigo() {
        return codigo;
    }

    public String getApartamentoId() {
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

    public VersionPolitica getVersionPolitica() {
        return versionPolitica;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public boolean isConMascota() {
        return conMascota;
    }

    /** Null mientras no se haya registrado (RN-09). */
    public LocalTime getHoraEstimadaLlegada() {
        return horaEstimadaLlegada;
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
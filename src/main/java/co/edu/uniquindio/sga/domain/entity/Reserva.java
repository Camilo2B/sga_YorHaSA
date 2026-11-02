package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.Cancelacion;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DetalleNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.Gravedad;
import co.edu.uniquindio.sga.domain.valueobject.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaAnticipo;
import co.edu.uniquindio.sga.domain.valueobject.RecargoLlegadaNocturna;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    private Cancelacion cancelacion;
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
        this.cancelacion = null;

        this.estado = EstadoReserva.PENDIENTE;
        this.folio = Folio.crear(this.id);
        this.ocupantes = new ArrayList<>();
        this.novedades = new ArrayList<>();

        inicializarCargosHospedaje();
    }

    private Reserva(UUID id, CodigoReserva codigo, String apartamentoId, Titular titular, Estancia estancia,
                    CanalOrigen canalOrigen, EstadoReserva estado, Cotizacion cotizacion, VersionPolitica versionPolitica,
                    LocalDateTime fechaCreacion, boolean conMascota, LocalTime horaEstimadaLlegada,
                    Cancelacion cancelacion, Folio folio, List<Ocupante> ocupantes, List<Novedad> novedades) {
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
        this.cancelacion = cancelacion;
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
                                      LocalTime horaEstimadaLlegada, Cancelacion cancelacion, Folio folio,
                                      List<Ocupante> ocupantes, List<Novedad> novedades) {
        return new Reserva(id, codigo, apartamentoId, titular, estancia, canalOrigen, estado, cotizacion,
                versionPolitica, fechaCreacion, conMascota, horaEstimadaLlegada, cancelacion, folio, ocupantes, novedades);
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
     * Confirma la reserva cobrando el anticipo (F-06, 7.5). Reglas: solo desde PENDIENTE (RN-08), con la hora estimada
     * de llegada registrada (RN-09), dentro del plazo de confirmación (RN-21) y pagando el anticipo que exija el
     * alojamiento sobre el valor de la estancia. Si algo falla no cambia nada: primero se valida todo y después se actúa.
     *
     * @return el pago del anticipo, o null si el alojamiento no exige anticipo
     */
    public Pago confirmarConAnticipo(PoliticaAnticipo politicaAnticipo, PlazoConfirmacion plazoConfirmacion,
                                     MedioPago medioPago, String referencia, LocalDateTime ahora) {
        Objects.requireNonNull(politicaAnticipo, "La política de anticipo no puede ser nula.");
        Objects.requireNonNull(plazoConfirmacion, "El plazo de confirmación no puede ser nulo.");
        Objects.requireNonNull(ahora, "El momento de la confirmación no puede ser nulo.");

        if (!this.estado.puedeTransitarA(EstadoReserva.CONFIRMADA)) {
            throw new ReglaDominioException("RN-08: solo se pueden confirmar reservas en estado PENDIENTE (estado actual: "
                    + this.estado + ").");
        }
        if (this.horaEstimadaLlegada == null) {
            throw new ReglaDominioException("RN-09: la reserva debe registrar la hora estimada de llegada antes de confirmarse.");
        }
        if (plazoConfirmacion.haVencido(this.fechaCreacion, ahora)) {
            throw new ReglaDominioException("RN-21: el plazo de confirmación de " + plazoConfirmacion.horas()
                    + " horas venció el " + this.fechaCreacion.plusHours(plazoConfirmacion.horas()) + ".");
        }
        Dinero anticipo = politicaAnticipo.calcularAnticipo(this.cotizacion.valorTotal());
        if (!anticipo.esCero() && medioPago == null) {
            throw new ReglaDominioException("Para confirmar hay que pagar el anticipo de " + anticipo.monto()
                    + " COP: indique el medio de pago.");
        }

        Pago pagoAnticipo = anticipo.esCero() ? null : this.folio.registrarPago(anticipo, medioPago, referencia, ahora.toLocalDate());
        this.estado = EstadoReserva.CONFIRMADA;
        return pagoAnticipo;
    }

    /**
     * Cancela la reserva. La retención se calcula sobre lo que el huésped ya pagó (el anticipo) con la versión de la
     * política CONGELADA en la reserva (RN-13), según la antelación entre ahora y el inicio del día de entrada. Dinero
     * no admite negativos, así que la devolución no se modela como un cargo inverso: queda registrada en la cancelación.
     * Libera las noches de inmediato (RN-12), porque una reserva CANCELADA deja de ser activa.
     */
    public Cancelacion cancelar(PoliticaCancelacion politica, String motivo, LocalDateTime ahora) {
        Objects.requireNonNull(politica, "La política de cancelación no puede ser nula.");
        Objects.requireNonNull(ahora, "El momento de la cancelación no puede ser nulo.");
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("El motivo de la cancelación es obligatorio.");
        }
        if (!this.estado.puedeTransitarA(EstadoReserva.CANCELADA)) {
            throw new ReglaDominioException("RN-08: no se puede cancelar una reserva en estado " + this.estado + ".");
        }
        if (!politica.numeroVersion().equals(this.versionPolitica)) {
            throw new ReglaDominioException("RN-13: la cancelación debe usar la versión " + this.versionPolitica.numero()
                    + " de la política, que es la congelada en esta reserva.");
        }

        long horasDeAntelacion = ChronoUnit.HOURS.between(ahora, this.estancia.entrada().atStartOfDay());
        BigDecimal porcentajeRetenido = politica.retencionAplicable(Math.max(0, horasDeAntelacion));
        Dinero pagado = this.folio.calcularTotalPagos();
        Dinero retenido = pagado.multiplicar(porcentajeRetenido);
        Dinero devuelto = pagado.restar(retenido);

        this.cancelacion = new Cancelacion(motivo.trim(), ahora, retenido, devuelto);
        this.estado = EstadoReserva.CANCELADA;
        return this.cancelacion;
    }

    /**
     * RN-15: registra un pago contra el folio. Solo mientras la reserva sigue viva y ya confirmada (CONFIRMADA o
     * EN_CURSO): el anticipo de una PENDIENTE se paga al confirmar, y en las terminales el folio ya no se mueve.
     * El folio rechaza pagar más que el saldo pendiente.
     */
    public Pago registrarPago(Dinero monto, MedioPago medioPago, String referencia, LocalDate fecha) {
        if (this.estado != EstadoReserva.CONFIRMADA && this.estado != EstadoReserva.EN_CURSO) {
            throw new ReglaDominioException("RN-15: solo se registran pagos en reservas CONFIRMADA o EN_CURSO (estado actual: "
                    + this.estado + ").");
        }
        return this.folio.registrarPago(monto, medioPago, referencia, fecha);
    }

    /** RN-12: una reserva CONFIRMADA cuyo titular no llegó antes de la hora límite del día de entrada es un no-show. */
    public boolean haPasadoHoraLimiteNoShow(HoraLimiteNoShow horaLimite, LocalDateTime ahora) {
        return this.estado == EstadoReserva.CONFIRMADA && horaLimite.permiteDeclarar(this.estancia.entrada(), ahora);
    }

    /**
     * RN-12 y RN-13: declara el no-show, libera las noches y retiene el anticipo según la versión de política CONGELADA
     * en la reserva (el tramo de 0 horas de antelación, que en la Ficha retiene el 100%). Solo si ya pasó la hora límite.
     */
    public Cancelacion declararNoShow(HoraLimiteNoShow horaLimite, PoliticaCancelacion politica, LocalDateTime ahora) {
        Objects.requireNonNull(horaLimite, "La hora límite de no-show no puede ser nula.");
        Objects.requireNonNull(politica, "La política de cancelación no puede ser nula.");
        Objects.requireNonNull(ahora, "El momento de la declaración no puede ser nulo.");
        if (!this.estado.puedeTransitarA(EstadoReserva.NO_SHOW)) {
            throw new ReglaDominioException("RN-08: solo se puede declarar no-show en una reserva CONFIRMADA (estado actual: "
                    + this.estado + ").");
        }
        if (!horaLimite.permiteDeclarar(this.estancia.entrada(), ahora)) {
            throw new ReglaDominioException("RN-12: todavía no ha pasado la hora límite de no-show (" + horaLimite.hora()
                    + " del " + this.estancia.entrada() + ").");
        }
        if (!politica.numeroVersion().equals(this.versionPolitica)) {
            throw new ReglaDominioException("RN-13: el no-show debe usar la versión " + this.versionPolitica.numero()
                    + " de la política, que es la congelada en esta reserva.");
        }

        BigDecimal porcentajeRetenido = politica.retencionAplicable(0);
        Dinero pagado = this.folio.calcularTotalPagos();
        Dinero retenido = pagado.multiplicar(porcentajeRetenido);
        Dinero devuelto = pagado.restar(retenido);

        this.cancelacion = new Cancelacion("NO_SHOW: el titular no se presentó antes de las " + horaLimite.hora()
                + " del día de entrada", ahora, retenido, devuelto);
        this.estado = EstadoReserva.NO_SHOW;
        return this.cancelacion;
    }

    /** RN-21: una reserva PENDIENTE que supera el plazo de confirmación debe cancelarse automáticamente. */
    public boolean haVencidoPlazoConfirmacion(PlazoConfirmacion plazoConfirmacion, LocalDateTime ahora) {
        return this.estado == EstadoReserva.PENDIENTE && plazoConfirmacion.haVencido(this.fechaCreacion, ahora);
    }

    /** RN-21 y RN-12: cancela la reserva vencida y libera sus noches. Nada se había pagado, así que no hay retención. */
    public Cancelacion cancelarPorVencimiento(PlazoConfirmacion plazoConfirmacion, LocalDateTime ahora) {
        if (!haVencidoPlazoConfirmacion(plazoConfirmacion, ahora)) {
            throw new ReglaDominioException("RN-21: la reserva " + this.codigo.valor()
                    + " no está PENDIENTE con el plazo de confirmación vencido.");
        }
        Dinero pagado = this.folio.calcularTotalPagos();
        this.cancelacion = new Cancelacion("RN-21: plazo de confirmación de " + plazoConfirmacion.horas() + " horas vencido",
                ahora, Dinero.cero(), pagado);
        this.estado = EstadoReserva.CANCELADA;
        return this.cancelacion;
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

    /** RN-02 / RN-Propia-03: cupos del apartamento que ocupa la reserva (todos los ocupantes y, si la trae, la mascota). */
    public int cuposNecesarios() {
        return ocupantes.size() + (conMascota ? 1 : 0);
    }

    /** Presente solo si la reserva fue cancelada. */
    public Optional<Cancelacion> getCancelacion() {
        return Optional.ofNullable(cancelacion);
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
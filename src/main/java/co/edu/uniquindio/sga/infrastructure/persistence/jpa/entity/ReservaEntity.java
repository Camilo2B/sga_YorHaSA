package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Persistencia del agregado Reserva (con su folio, ocupantes y novedades). Es una clase de infraestructura separada
 * del dominio: el dominio no sabe que existe. El apartamento se guarda solo por su identificador (referencia entre
 * agregados, sin llave foránea a su entidad) y la cotización queda congelada como copia de valores (RN-22).
 */
@Entity
@Table(name = "reserva")
public class ReservaEntity {

    @Id
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "apartamento_id", nullable = false, length = 40)
    private String apartamentoId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "documento", column = @Column(name = "titular_documento", nullable = false)),
            @AttributeOverride(name = "nombre", column = @Column(name = "titular_nombre", nullable = false)),
            @AttributeOverride(name = "fechaNacimiento", column = @Column(name = "titular_fecha_nacimiento", nullable = false))
    })
    private OcupanteEmbeddable titular;

    @Embedded
    private EstanciaEmbeddable estancia;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origen", nullable = false)
    private CanalOrigen canalOrigen;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoReserva estado;

    @Column(name = "version_politica_numero", nullable = false)
    private int versionPoliticaNumero;

    @Column(name = "version_politica_vigente_desde", nullable = false)
    private LocalDate versionPoliticaVigenteDesde;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "con_mascota", nullable = false)
    private boolean conMascota;

    @Column(name = "hora_estimada_llegada")
    private LocalTime horaEstimadaLlegada;

    @Column(name = "motivo_cancelacion", length = 300)
    private String motivoCancelacion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "monto_retenido", precision = 19, scale = 0)
    private BigDecimal montoRetenido;

    @Column(name = "monto_devuelto", precision = 19, scale = 0)
    private BigDecimal montoDevuelto;

    @Column(name = "folio_id", nullable = false)
    private UUID folioId;

    @ElementCollection
    @CollectionTable(name = "reserva_detalle_noche", joinColumns = @JoinColumn(name = "reserva_id"))
    @OrderBy("fecha ASC")
    private List<DetalleNocheEmbeddable> detalleNoches = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "reserva_ocupante", joinColumns = @JoinColumn(name = "reserva_id"))
    private List<OcupanteEmbeddable> ocupantes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "reserva_cargo", joinColumns = @JoinColumn(name = "reserva_id"))
    private List<CargoEmbeddable> cargos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "reserva_pago", joinColumns = @JoinColumn(name = "reserva_id"))
    private List<PagoEmbeddable> pagos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "reserva_novedad", joinColumns = @JoinColumn(name = "reserva_id"))
    private List<NovedadReservaEmbeddable> novedades = new ArrayList<>();

    protected ReservaEntity() {
    }

    public ReservaEntity(UUID id, String codigo, String apartamentoId, OcupanteEmbeddable titular, EstanciaEmbeddable estancia,
                         CanalOrigen canalOrigen, EstadoReserva estado, int versionPoliticaNumero,
                         LocalDate versionPoliticaVigenteDesde, LocalDateTime fechaCreacion, boolean conMascota,
                         LocalTime horaEstimadaLlegada, UUID folioId) {
        this.id = id;
        this.codigo = codigo;
        this.apartamentoId = apartamentoId;
        this.titular = titular;
        this.estancia = estancia;
        this.canalOrigen = canalOrigen;
        this.estado = estado;
        this.versionPoliticaNumero = versionPoliticaNumero;
        this.versionPoliticaVigenteDesde = versionPoliticaVigenteDesde;
        this.fechaCreacion = fechaCreacion;
        this.conMascota = conMascota;
        this.horaEstimadaLlegada = horaEstimadaLlegada;
        this.folioId = folioId;
    }

    /** Las cuatro columnas quedan vacías mientras la reserva no se cancele. */
    public void asignarCancelacion(String motivo, LocalDateTime fecha, BigDecimal retenido, BigDecimal devuelto) {
        this.motivoCancelacion = motivo;
        this.fechaCancelacion = fecha;
        this.montoRetenido = retenido;
        this.montoDevuelto = devuelto;
    }

    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getApartamentoId() { return apartamentoId; }
    public OcupanteEmbeddable getTitular() { return titular; }
    public EstanciaEmbeddable getEstancia() { return estancia; }
    public CanalOrigen getCanalOrigen() { return canalOrigen; }
    public EstadoReserva getEstado() { return estado; }
    public int getVersionPoliticaNumero() { return versionPoliticaNumero; }
    public LocalDate getVersionPoliticaVigenteDesde() { return versionPoliticaVigenteDesde; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public boolean isConMascota() { return conMascota; }
    public LocalTime getHoraEstimadaLlegada() { return horaEstimadaLlegada; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public LocalDateTime getFechaCancelacion() { return fechaCancelacion; }
    public BigDecimal getMontoRetenido() { return montoRetenido; }
    public BigDecimal getMontoDevuelto() { return montoDevuelto; }
    public UUID getFolioId() { return folioId; }
    public List<DetalleNocheEmbeddable> getDetalleNoches() { return detalleNoches; }
    public List<OcupanteEmbeddable> getOcupantes() { return ocupantes; }
    public List<CargoEmbeddable> getCargos() { return cargos; }
    public List<PagoEmbeddable> getPagos() { return pagos; }
    public List<NovedadReservaEmbeddable> getNovedades() { return novedades; }
}

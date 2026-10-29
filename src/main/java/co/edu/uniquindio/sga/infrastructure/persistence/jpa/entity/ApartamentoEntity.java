package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA del agregado Apartamento. Vive solo en infraestructura: el dominio no sabe que existe.
 * La clave primaria es la identificación de negocio (ej. "ATK-001"), sin id sintético.
 * La eliminación es lógica: la columna "activo" nunca se borra.
 */
@Entity
@Table(name = "apartamento")
public class ApartamentoEntity {

    @Id
    @Column(name = "identificacion", length = 40)
    private String identificacion;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "capacidad", nullable = false)
    private int capacidad;

    @Column(name = "tipo_nombre")
    private String tipoNombre;

    @ElementCollection
    @CollectionTable(name = "apartamento_tipo_caracteristica", joinColumns = @JoinColumn(name = "apartamento_id"))
    @Column(name = "caracteristica")
    private List<String> tipoCaracteristicas = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "apartamento_dormitorio", joinColumns = @JoinColumn(name = "apartamento_id"))
    private List<DormitorioEmbeddable> dormitorios = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_operativo", nullable = false)
    private EstadoOperativo estadoOperativo;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @OneToMany(mappedBy = "apartamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TarifaEntity> tarifas = new ArrayList<>();

    @OneToMany(mappedBy = "apartamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BloqueoEntity> bloqueos = new ArrayList<>();

    @OneToMany(mappedBy = "apartamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NovedadEntity> novedades = new ArrayList<>();

    /** Requerido por JPA. */
    protected ApartamentoEntity() {
    }

    public ApartamentoEntity(String identificacion, String nombre, int capacidad, String tipoNombre,
                             List<String> tipoCaracteristicas, EstadoOperativo estadoOperativo, boolean activo) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.tipoNombre = tipoNombre;
        this.tipoCaracteristicas = new ArrayList<>(tipoCaracteristicas);
        this.estadoOperativo = estadoOperativo;
        this.activo = activo;
    }

    public void agregarTarifa(TarifaEntity tarifa) {
        tarifa.asignarApartamento(this);
        tarifas.add(tarifa);
    }

    public void agregarBloqueo(BloqueoEntity bloqueo) {
        bloqueo.asignarApartamento(this);
        bloqueos.add(bloqueo);
    }

    public void agregarNovedad(NovedadEntity novedad) {
        novedad.asignarApartamento(this);
        novedades.add(novedad);
    }

    public String getIdentificacion() { return identificacion; }
    public String getNombre() { return nombre; }
    public int getCapacidad() { return capacidad; }
    public String getTipoNombre() { return tipoNombre; }
    public List<String> getTipoCaracteristicas() { return tipoCaracteristicas; }
    public List<DormitorioEmbeddable> getDormitorios() { return dormitorios; }
    public EstadoOperativo getEstadoOperativo() { return estadoOperativo; }
    public boolean isActivo() { return activo; }
    public List<TarifaEntity> getTarifas() { return tarifas; }
    public List<BloqueoEntity> getBloqueos() { return bloqueos; }
    public List<NovedadEntity> getNovedades() { return novedades; }
}

package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Una fila por versión de la política: las versiones anteriores nunca se editan ni se borran (F-07, RN-13). */
@Entity
@Table(name = "politica_cancelacion")
public class PoliticaCancelacionEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alojamiento_id", nullable = false)
    private AlojamientoEntity alojamiento;

    @Column(name = "numero_version", nullable = false)
    private int numeroVersion;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;

    @ElementCollection
    @CollectionTable(name = "politica_cancelacion_tramo", joinColumns = @JoinColumn(name = "politica_id"))
    private List<TramoEmbeddable> tramos = new ArrayList<>();

    protected PoliticaCancelacionEntity() {
    }

    public PoliticaCancelacionEntity(UUID id, int numeroVersion, LocalDate vigenteDesde, List<TramoEmbeddable> tramos) {
        this.id = id;
        this.numeroVersion = numeroVersion;
        this.vigenteDesde = vigenteDesde;
        this.tramos = new ArrayList<>(tramos);
    }

    void asignarAlojamiento(AlojamientoEntity alojamiento) {
        this.alojamiento = alojamiento;
    }

    public UUID getId() { return id; }
    public int getNumeroVersion() { return numeroVersion; }
    public LocalDate getVigenteDesde() { return vigenteDesde; }
    public List<TramoEmbeddable> getTramos() { return tramos; }
}

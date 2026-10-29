package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "bloqueo")
public class BloqueoEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartamento_id", nullable = false)
    private ApartamentoEntity apartamento;

    @Embedded
    private EstanciaEmbeddable rango;

    @Column(name = "motivo", nullable = false)
    private String motivo;

    protected BloqueoEntity() {
    }

    public BloqueoEntity(UUID id, EstanciaEmbeddable rango, String motivo) {
        this.id = id;
        this.rango = rango;
        this.motivo = motivo;
    }

    void asignarApartamento(ApartamentoEntity apartamento) {
        this.apartamento = apartamento;
    }

    public UUID getId() { return id; }
    public EstanciaEmbeddable getRango() { return rango; }
    public String getMotivo() { return motivo; }
}

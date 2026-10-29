package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.Gravedad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "novedad")
public class NovedadEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartamento_id", nullable = false)
    private ApartamentoEntity apartamento;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "autor", nullable = false)
    private String autor;

    @Column(name = "descripcion", nullable = false, length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "gravedad", nullable = false)
    private Gravedad gravedad;

    protected NovedadEntity() {
    }

    public NovedadEntity(UUID id, LocalDate fecha, String autor, String descripcion, Gravedad gravedad) {
        this.id = id;
        this.fecha = fecha;
        this.autor = autor;
        this.descripcion = descripcion;
        this.gravedad = gravedad;
    }

    void asignarApartamento(ApartamentoEntity apartamento) {
        this.apartamento = apartamento;
    }

    public UUID getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public String getAutor() { return autor; }
    public String getDescripcion() { return descripcion; }
    public Gravedad getGravedad() { return gravedad; }
}

package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/** Las fechas son nulas en la temporada base (cubre todo lo que no cubra otra temporada). */
@Entity
@Table(name = "temporada")
public class TemporadaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alojamiento_id", nullable = false)
    private AlojamientoEntity alojamiento;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "es_base", nullable = false)
    private boolean esBase;

    protected TemporadaEntity() {
    }

    public TemporadaEntity(UUID id, String nombre, LocalDate fechaInicio, LocalDate fechaFin, boolean esBase) {
        this.id = id;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.esBase = esBase;
    }

    void asignarAlojamiento(AlojamientoEntity alojamiento) {
        this.alojamiento = alojamiento;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public boolean isEsBase() { return esBase; }
}

package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA del agregado Alojamiento. Guarda solo los DATOS que cambian por la API (identidad, temporadas y
 * políticas). Los parámetros de la Ficha no se guardan: viven en application.properties (única fuente).
 */
@Entity
@Table(name = "alojamiento")
public class AlojamientoEntity {

    @Id
    private UUID id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "ciudad", nullable = false)
    private String ciudad;

    @OneToMany(mappedBy = "alojamiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TemporadaEntity> temporadas = new ArrayList<>();

    @OneToMany(mappedBy = "alojamiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PoliticaCancelacionEntity> politicas = new ArrayList<>();

    protected AlojamientoEntity() {
    }

    public AlojamientoEntity(UUID id, String nombre, String ciudad) {
        this.id = id;
        this.nombre = nombre;
        this.ciudad = ciudad;
    }

    public void agregarTemporada(TemporadaEntity temporada) {
        temporada.asignarAlojamiento(this);
        temporadas.add(temporada);
    }

    public void agregarPolitica(PoliticaCancelacionEntity politica) {
        politica.asignarAlojamiento(this);
        politicas.add(politica);
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCiudad() { return ciudad; }
    public List<TemporadaEntity> getTemporadas() { return temporadas; }
    public List<PoliticaCancelacionEntity> getPoliticas() { return politicas; }
}

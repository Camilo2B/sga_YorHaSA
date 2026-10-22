package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;

import java.time.LocalDateTime;
import java.util.Objects;

public class ConflictoCanal {

    private String id;
    private Reserva reservaExterna;
    private Reserva reservaVigente;
    private Apartamento apartamentoid;
    private LocalDateTime fechaHoraIntento;
    private EstadoOperativo estadoRevision;
    private String observacion;

    public ConflictoCanal(String id, Reserva reservaExterna, Reserva reservaVigente, Apartamento apartamentoid, LocalDateTime fechaHoraIntento,
                          EstadoOperativo estadoRevision, String observacion) {
        if (id == null) {
            throw new ReglaDominioException("El id no puede ser vacio");

        }
        if (reservaExterna == null) {
            throw new ReglaDominioException("La reserva externa no puede ser vacia");
        }

        if (reservaVigente == null) {
            throw new ReglaDominioException("La reserva Vigente no puede ser vacia");
        }

        if (apartamentoid == null) {
            throw new ReglaDominioException("El id del apartamento no puede ser vacio");
        }

        if (fechaHoraIntento == null) {
            throw new ReglaDominioException("La fecha y hora del intento no puede ser vacio");
        }

        if (estadoRevision == null) {
            throw new ReglaDominioException("El estado de la revisión no puede ser vacia");
        }

        if (observacion == null) {
            throw new ReglaDominioException("La observación no puede ser vacia");
        }
        this.id = id;
        this.reservaExterna = reservaExterna;
        this.reservaVigente = reservaVigente;
        this.apartamentoid = apartamentoid;
        this.fechaHoraIntento = fechaHoraIntento;
        this.estadoRevision = estadoRevision;
        this.observacion = observacion;
    }

    public String getId() {
        return id;
    }

    public Reserva getReservaExterna() {
        return reservaExterna;
    }

    public Reserva getReservaVigente() {
        return reservaVigente;
    }

    public Apartamento getApartamentoid() {
        return apartamentoid;
    }

    public LocalDateTime getFechaHoraIntento() {
        return fechaHoraIntento;
    }

    public EstadoOperativo getEstadoRevision() {
        return estadoRevision;
    }

    public String getObservacion() {
        return observacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConflictoCanal)) return false;
        return Objects.equals(id, ((ConflictoCanal) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

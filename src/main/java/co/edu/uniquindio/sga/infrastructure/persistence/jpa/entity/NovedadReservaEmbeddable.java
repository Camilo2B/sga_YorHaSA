package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.Gravedad;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;
import java.util.UUID;

/** Una novedad registrada durante la estancia de la reserva. */
@Embeddable
public record NovedadReservaEmbeddable(
        UUID id,
        LocalDate fecha,
        String autor,
        @Column(length = 1000) String descripcion,
        @Enumerated(EnumType.STRING) Gravedad gravedad) {
}

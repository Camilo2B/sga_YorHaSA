package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Un pago registrado en el folio de la reserva. */
@Embeddable
public record PagoEmbeddable(
        UUID id,
        @Enumerated(EnumType.STRING) MedioPago medio,
        LocalDate fecha,
        @Column(precision = 19, scale = 0) BigDecimal valor) {
}

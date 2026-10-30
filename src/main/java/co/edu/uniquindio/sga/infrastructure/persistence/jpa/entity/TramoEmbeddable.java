package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

/** Persistencia del Value Object TramoAntelacion. La devolución no se guarda: es el complemento de la retención. */
@Embeddable
public record TramoEmbeddable(
        int antelacionMinimaHoras,
        @Column(precision = 5, scale = 4) BigDecimal porcentajeRetencion) {
}

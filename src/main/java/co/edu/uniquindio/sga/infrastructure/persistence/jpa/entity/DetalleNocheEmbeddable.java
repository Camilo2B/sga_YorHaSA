package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Una noche del desglose de la cotización congelada en la reserva (RN-22): copia de valores, sin referencias vivas. */
@Embeddable
public record DetalleNocheEmbeddable(
        LocalDate fecha,
        @Column(name = "nombre_temporada") String nombreTemporada,
        @Column(name = "tarifa_aplicada", precision = 19, scale = 0) BigDecimal tarifaAplicada,
        @Column(name = "ocupantes_facturables") int ocupantesFacturables) {
}

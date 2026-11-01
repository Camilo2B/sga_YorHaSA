package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.MedioPago;

import java.time.LocalDate;
import java.util.UUID;

/** Un pago registrado en el folio, en pesos colombianos sin decimales. */
public record PagoResponse(UUID id, MedioPago medio, LocalDate fecha, long valor) {
}

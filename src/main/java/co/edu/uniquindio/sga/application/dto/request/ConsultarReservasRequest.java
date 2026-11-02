package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDate;

/**
 * Filtros opcionales de GET /api/reservas (7.5). Si se indican desde y hasta, se traen las reservas con al menos una
 * noche en [desde, hasta).
 */
public record ConsultarReservasRequest(
        EstadoReserva estado,
        String identificacionApartamento,
        LocalDate desde,
        LocalDate hasta
) {

    @AssertTrue(message = "La fecha 'hasta' debe ser posterior a la fecha 'desde'")
    public boolean isRangoValido() {
        return desde == null || hasta == null || hasta.isAfter(desde);
    }
}

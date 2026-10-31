package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.List;

/** Respuesta de POST /api/cotizaciones. valorTotal en pesos sin decimales (3.4). */
public record CotizacionResponse(
        String identificacionApartamento,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        int noches,
        int ocupantesFacturables,
        List<DetalleNocheResponse> desglose,
        long valorTotal,
        String moneda
) {
}

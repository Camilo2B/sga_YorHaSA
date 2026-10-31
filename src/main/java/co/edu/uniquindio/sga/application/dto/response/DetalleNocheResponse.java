package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;

/** Una noche de la cotización. Los valores son pesos colombianos sin decimales (3.4). */
public record DetalleNocheResponse(LocalDate fecha, String temporada, long tarifaAplicada,
                                   int ocupantesFacturables, long subtotal) {
}

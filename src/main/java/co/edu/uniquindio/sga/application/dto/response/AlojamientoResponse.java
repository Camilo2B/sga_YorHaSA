package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** GET /api/alojamiento. */
public record AlojamientoResponse(
        String nombre,
        String ciudad,
        ParametrosResponse parametros,
        List<TemporadaResponse> temporadas,
        PoliticaCancelacionResponse politicaVigente
) {
}

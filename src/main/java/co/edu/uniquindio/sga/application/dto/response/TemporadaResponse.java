package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.UUID;

/** Las fechas son nulas en la temporada base: cubre todo lo que no cubra otra temporada. */
public record TemporadaResponse(UUID id, String nombre, LocalDate fechaInicio, LocalDate fechaFin, boolean esBase) {
}

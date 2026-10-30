package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** POST /api/alojamiento/temporadas. La temporada base no se crea por la API: nace con el alojamiento. */
public record RegistrarTemporadaRequest(
        @NotBlank(message = "El nombre de la temporada es obligatorio")
        String nombre,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin
) {
}

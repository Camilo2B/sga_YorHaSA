package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * PUT /api/apartamentos/{identificacion}/capacidad. Valida solo la forma (400); que ninguna reserva activa quede por
 * encima de la nueva capacidad lo valida el dominio (409).
 */
public record CambiarCapacidadRequest(
        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad debe ser al menos 1")
        Integer capacidad
) {
}

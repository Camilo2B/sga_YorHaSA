package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * POST /api/cotizaciones. Valida solo la forma (400); las reglas (salida posterior a la entrada, tarifa existente,
 * al menos un ocupante facturable) las valida el dominio (409). Los ocupantes usan el mismo formato que la reserva.
 */
public record CotizarEstanciaRequest(
        @NotBlank(message = "La identificación del apartamento es obligatoria")
        String identificacionApartamento,

        @NotNull(message = "La fecha de entrada es obligatoria")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaSalida,

        @NotEmpty(message = "Debe indicar al menos un ocupante")
        @Valid
        List<@NotNull(message = "Un ocupante no puede ser nulo") OcupanteRequest> ocupantes
) {
}

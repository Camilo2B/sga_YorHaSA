package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * POST /api/alojamiento/politica-cancelacion. Valida la forma (400); las reglas (mínimo dos tramos, tramo de 0 horas,
 * vigencia posterior a la versión más reciente) las valida el dominio (409).
 */
public record DefinirPoliticaCancelacionRequest(
        @NotEmpty(message = "La política debe tener tramos")
        @Valid
        List<@NotNull(message = "Un tramo no puede ser nulo") TramoRequest> tramos,

        @NotNull(message = "La fecha desde la que rige la política es obligatoria")
        LocalDate vigenteDesde
) {
}

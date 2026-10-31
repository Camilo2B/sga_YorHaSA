package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * POST /api/apartamentos/{identificacion}/tarifas. Valida la forma del mensaje (400); que la tarifa sea positiva
 * y rija después de la anterior lo valida el dominio (409).
 */
public record DefinirTarifaRequest(
        @NotNull(message = "La temporada es obligatoria")
        UUID temporadaId,

        @NotNull(message = "El valor por noche es obligatorio")
        @Positive(message = "El valor por noche debe ser mayor a cero")
        BigDecimal valorPorNoche,

        @NotNull(message = "La fecha desde la que rige la tarifa es obligatoria")
        LocalDate vigenteDesde
) {
}

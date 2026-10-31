package co.edu.uniquindio.sga.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Una versión de la tarifa de un apartamento: valor por ocupante facturable y por noche, en COP. */
public record TarifaResponse(UUID id, UUID temporadaId, BigDecimal valorPorNoche, LocalDate vigenteDesde) {
}

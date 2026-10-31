package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Un tramo de la política: desde cierta antelación mínima (en horas) se retiene un porcentaje del anticipo. */
public record TramoRequest(
        @NotNull(message = "La antelación mínima es obligatoria")
        @Min(value = 0, message = "La antelación mínima no puede ser negativa")
        Integer antelacionMinimaHoras,

        @NotNull(message = "El porcentaje de retención es obligatorio")
        @DecimalMin(value = "0", message = "El porcentaje de retención debe estar entre 0 y 1")
        @DecimalMax(value = "1", message = "El porcentaje de retención debe estar entre 0 y 1")
        BigDecimal porcentajeRetencion
) {
}

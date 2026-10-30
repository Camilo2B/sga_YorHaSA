package co.edu.uniquindio.sga.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalTime;

public record ParametrosResponse(
        int umbralEdadFacturable,
        LocalTime horaEntrada,
        LocalTime horaSalida,
        int tiempoPreparacionHoras,
        int plazoConfirmacionHoras,
        LocalTime horaLimiteNoShow,
        boolean exigeAnticipo,
        BigDecimal porcentajeAnticipo
) {
}

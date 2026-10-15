package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CrearReservaRequest(
    @NotBlank(message = "La identificacion del apartamento es obligatorio")
    String identificacionApartamento,
    @NotNull()
    LocalDate fechaEntrada,
    LocalDate fechaSalida
    String canal,
    LocalTime horaEstimadaLlegada,
    OcupanteRequest titular,
    List<OcupanteRequest> ocupantes
    )
{
}

package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * POST /api/reservas. Valida solo la forma del mensaje (400); las reglas (fecha pasada, capacidad, solapamiento,
 * bloqueos...) las valida el dominio (409). El titular es siempre un ocupante más (3.2): "ocupantes" son sus
 * acompañantes y puede ir vacía.
 */
public record CrearReservaRequest(
        @NotBlank(message = "La identificación del apartamento es obligatoria")
        String identificacionApartamento,

        @NotNull(message = "La fecha de entrada es obligatoria")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaSalida,

        @NotNull(message = "El canal de origen es obligatorio")
        CanalOrigen canalOrigen,

        // Opcional al crear; es obligatoria antes de confirmar (RN-09).
        LocalTime horaEstimadaLlegada,

        // Si no se envía se asume false. Con mascota se ocupa un cupo del apartamento (RN-Propia-03).
        Boolean conMascota,

        @NotNull(message = "La reserva debe tener titular")
        @Valid
        OcupanteRequest titular,

        @NotNull(message = "La lista de acompañantes es obligatoria (puede ir vacía)")
        @Valid
        List<@NotNull(message = "Un acompañante no puede ser nulo") OcupanteRequest> ocupantes
) {
}

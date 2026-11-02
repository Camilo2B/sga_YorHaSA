package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Parámetros de GET /api/apartamentos/disponibles (7.5): fechas y composición del grupo. Valida solo la forma (400);
 * las reglas (salida posterior a la entrada, capacidad, mascotas, reservas, bloqueos y preparación) las valida el
 * dominio. Todos los ocupantes cuentan para la capacidad; los niños solo se separan para describir al grupo.
 */
public record BuscarDisponibilidadRequest(
        @NotNull(message = "La fecha de entrada es obligatoria")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaSalida,

        @NotNull(message = "El número de adultos es obligatorio")
        @Min(value = 1, message = "Debe haber al menos un adulto (el titular)")
        Integer adultos,

        // Si no se envía se asume 0.
        @Min(value = 0, message = "El número de niños no puede ser negativo")
        Integer ninos,

        // Si no se envía se asume false. Con mascota se ocupa un cupo del apartamento (RN-Propia-03).
        Boolean conMascota
) {
}

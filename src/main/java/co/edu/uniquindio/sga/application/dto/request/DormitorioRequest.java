package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DormitorioRequest(
        @NotBlank(message = "La descripción del dormitorio es obligatoria")
        String descripcion,

        @NotBlank(message = "El tipo de cama del dormitorio es obligatorio")
        String tipoCama
) {
}

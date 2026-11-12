package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO de entrada del registro público de huéspedes (7.2). Valida solo la forma del mensaje (400);
 * que el correo o el documento ya estén registrados es una regla de negocio (409).
 */
public record RegistrarHuespedRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 120, message = "El correo no puede superar los 120 caracteres")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
                message = "La contraseña debe tener al menos una mayúscula y un número")
        String contrasena,

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String telefono,

        @NotBlank(message = "El documento es obligatorio")
        @Size(min = 7, max = 11, message = "El documento debe tener entre 7 y 11 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "El documento solo admite letras y números")
        String documento,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento no puede ser futura")
        LocalDate fechaNacimiento
) {
}

package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;

/** Información no sensible de una cuenta interna. Nunca expone la contraseña ni su hash. */
public record UsuarioInternoResponse(
        Long id,
        String nombre,
        String correo,
        String telefono,
        String documento,
        LocalDate fechaNacimiento,
        String rol,
        boolean activo
) {
}

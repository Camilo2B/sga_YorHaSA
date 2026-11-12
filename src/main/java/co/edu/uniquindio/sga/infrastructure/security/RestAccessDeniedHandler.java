package co.edu.uniquindio.sga.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 403 que se produce en la cadena de filtros (por ejemplo, una regla por URL). El 403 de @PreAuthorize lo
 * responde GlobalExceptionHandler; este responde el mismo mensaje y formato para que ambos sean indistinguibles.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"codigoError\":\"ACCESO_DENEGADO\","
                + "\"mensaje\":\"No tiene permiso para realizar esta operación\","
                + "\"detalles\":[]}");
    }
}

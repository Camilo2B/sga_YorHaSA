package co.edu.uniquindio.sga.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 401 sin token, con token inválido o vencido. Ocurre en la cadena de filtros, antes de llegar a un controller,
 * así que GlobalExceptionHandler no lo ve: sin esta clase la respuesta saldría con el cuerpo vacío.
 * Responde con el mismo formato ErrorResponse del resto de la API (codigoError, mensaje, detalles).
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Conserva el encabezado WWW-Authenticate: Bearer que exige el estándar para un 401 con JWT
    private final BearerTokenAuthenticationEntryPoint delegado = new BearerTokenAuthenticationEntryPoint();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        delegado.commence(request, response, authException);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // Texto fijo y sin comillas: se escribe a mano, sin depender de un serializador
        response.getWriter().write("{\"codigoError\":\"NO_AUTENTICADO\","
                + "\"mensaje\":\"Debe iniciar sesión con un token válido para acceder a este recurso\","
                + "\"detalles\":[]}");
    }
}

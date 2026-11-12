package co.edu.uniquindio.sga.infrastructure.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Base de las pruebas de la API REST: levanta la aplicación completa en un puerto libre (con la cadena de seguridad,
 * H2 y los usuarios de prueba reales) y habla con ella por HTTP, igual que lo haría el frontend.
 * Usa solo el cliente HTTP del JDK: no necesita librerías de prueba adicionales.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class PruebaRestBase {

    private static final Pattern TOKEN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern CODIGO = Pattern.compile("\"codigo\"\\s*:\\s*\"([^\"]+)\"");

    @Value("${local.server.port}")
    private int puerto;

    // HTTP/1.1 de forma explícita: evita el intento de actualización a h2c, que no aporta nada en pruebas
    private final HttpClient cliente = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    /** Envía una petición JSON. Con token == null no lleva Authorization; con cuerpo == null no lleva cuerpo. */
    protected HttpResponse<String> enviar(String metodo, String ruta, String token, String cuerpo) {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Content-Type", "application/json");
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        peticion.method(metodo, cuerpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(cuerpo));
        try {
            return cliente.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La petición fue interrumpida", e);
        }
    }

    protected static String credenciales(String correo, String contrasena) {
        return "{\"correo\":\"" + correo + "\",\"contrasena\":\"" + contrasena + "\"}";
    }

    /** Inicia sesión con un usuario de prueba y devuelve su JWT. */
    protected String iniciarSesion(String correo, String contrasena) {
        HttpResponse<String> respuesta = enviar("POST", "/api/auth/login", null, credenciales(correo, contrasena));
        assertEquals(200, respuesta.statusCode(), "Login de " + correo + ": " + respuesta.body());
        return extraer(TOKEN, respuesta.body());
    }

    /** Código de la reserva (RES-AAAA-NNNNN) que viene en el cuerpo de una respuesta. */
    protected static String codigoDeReserva(String cuerpo) {
        return extraer(CODIGO, cuerpo);
    }

    private static String extraer(Pattern patron, String texto) {
        Matcher coincidencia = patron.matcher(texto);
        if (!coincidencia.find()) {
            fail("No se encontró el dato esperado en la respuesta: " + texto);
        }
        return coincidencia.group(1);
    }

    /** Debe responder 403 con el ErrorResponse estándar (ACCESO_DENEGADO). */
    protected void assertProhibido(String token, String metodo, String ruta, String cuerpo) {
        HttpResponse<String> respuesta = enviar(metodo, ruta, token, cuerpo);
        assertEquals(403, respuesta.statusCode(), metodo + " " + ruta + " debía ser 403: " + respuesta.body());
        assertTrue(respuesta.body().contains("ACCESO_DENEGADO"), metodo + " " + ruta + ": " + respuesta.body());
    }

    /** La seguridad deja pasar la petición: que luego responda 200, 404 o 409 depende de los datos, no del rol. */
    protected void assertPermitido(String token, String metodo, String ruta, String cuerpo) {
        HttpResponse<String> respuesta = enviar(metodo, ruta, token, cuerpo);
        assertNotEquals(401, respuesta.statusCode(), metodo + " " + ruta + ": " + respuesta.body());
        assertNotEquals(403, respuesta.statusCode(), metodo + " " + ruta + " no debía ser 403: " + respuesta.body());
    }
}

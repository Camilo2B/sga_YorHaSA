package co.edu.uniquindio.sga.infrastructure.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Login, registro público de huéspedes (7.2) y rechazo de peticiones sin credenciales válidas. */
class AutenticacionRestTest extends PruebaRestBase {

    private static final String CONTRASENA_VALIDA = "Huesped2026";

    private static String correoNuevo() {
        return "huesped-" + UUID.randomUUID().toString().substring(0, 8) + "@prueba.local";
    }

    // 10 dígitos que empiezan por 9: no chocan con los documentos de los usuarios iniciales
    private static String documentoNuevo() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(9_000_000_000L, 9_999_999_999L));
    }

    private static String registro(String correo, String contrasena, String documento) {
        return """
                {
                  "nombre": "Huésped de prueba",
                  "correo": "%s",
                  "contrasena": "%s",
                  "telefono": "3001234567",
                  "documento": "%s",
                  "fechaNacimiento": "1995-04-20"
                }
                """.formatted(correo, contrasena, documento);
    }

    // ---------------- Login ----------------

    @Test
    @DisplayName("El login devuelve token, tipo, expiración y roles con los nombres de campo que usa el frontend")
    void loginDevuelveElContratoDelFrontend() {
        HttpResponse<String> respuesta = enviar("POST", "/api/auth/login", null,
                credenciales("admin@sga.local", "Admin2026"));

        assertEquals(200, respuesta.statusCode(), respuesta.body());
        for (String campo : new String[]{"\"token\"", "\"tipo\"", "\"expiraEn\"", "\"roles\"", "ADMINISTRADOR"}) {
            assertTrue(respuesta.body().contains(campo), "Falta " + campo + " en: " + respuesta.body());
        }
    }

    @Test
    @DisplayName("Contraseña incorrecta: 401 con ErrorResponse, sin revelar si el correo existe")
    void loginConContrasenaIncorrectaResponde401() {
        HttpResponse<String> respuesta = enviar("POST", "/api/auth/login", null,
                credenciales("admin@sga.local", "no-es-la-clave"));

        assertEquals(401, respuesta.statusCode(), respuesta.body());
        assertTrue(respuesta.body().contains("NO_AUTENTICADO"), respuesta.body());
    }

    // ---------------- Registro público ----------------

    @Test
    @DisplayName("Registro válido: 201 sin necesidad de token, y el nuevo huésped ya puede iniciar sesión con rol HUESPED")
    void registroValidoCreaUnHuespedQuePuedeIniciarSesion() {
        String correo = correoNuevo();

        HttpResponse<String> registro = enviar("POST", "/api/auth/registro", null,
                registro(correo, CONTRASENA_VALIDA, documentoNuevo()));
        assertEquals(201, registro.statusCode(), registro.body());

        HttpResponse<String> login = enviar("POST", "/api/auth/login", null, credenciales(correo, CONTRASENA_VALIDA));
        assertEquals(200, login.statusCode(), login.body());
        assertTrue(login.body().contains("HUESPED"), login.body());
        assertTrue(!login.body().contains("ADMINISTRADOR"), "El registro público solo crea huéspedes: " + login.body());
    }

    @Test
    @DisplayName("Registro con un correo ya registrado (aunque cambien las mayúsculas): 409")
    void registroConCorreoRepetidoResponde409() {
        String correo = correoNuevo();
        assertEquals(201, enviar("POST", "/api/auth/registro", null,
                registro(correo, CONTRASENA_VALIDA, documentoNuevo())).statusCode());

        HttpResponse<String> repetido = enviar("POST", "/api/auth/registro", null,
                registro(correo.toUpperCase(), CONTRASENA_VALIDA, documentoNuevo()));

        assertEquals(409, repetido.statusCode(), repetido.body());
        assertTrue(repetido.body().contains("REGLA_DOMINIO"), repetido.body());
    }

    @Test
    @DisplayName("Registro con un documento ya registrado: 409")
    void registroConDocumentoRepetidoResponde409() {
        String documento = documentoNuevo();
        assertEquals(201, enviar("POST", "/api/auth/registro", null,
                registro(correoNuevo(), CONTRASENA_VALIDA, documento)).statusCode());

        HttpResponse<String> repetido = enviar("POST", "/api/auth/registro", null,
                registro(correoNuevo(), CONTRASENA_VALIDA, documento));

        assertEquals(409, repetido.statusCode(), repetido.body());
        assertTrue(repetido.body().contains("REGLA_DOMINIO"), repetido.body());
    }

    @Test
    @DisplayName("Contraseña sin mayúscula ni número: 400 que señala el campo contrasena")
    void registroConContrasenaDebilResponde400() {
        HttpResponse<String> respuesta = enviar("POST", "/api/auth/registro", null,
                registro(correoNuevo(), "debilucha", documentoNuevo()));

        assertEquals(400, respuesta.statusCode(), respuesta.body());
        assertTrue(respuesta.body().contains("VALIDACION"), respuesta.body());
        assertTrue(respuesta.body().contains("contrasena"), respuesta.body());
    }

    @Test
    @DisplayName("Documento con caracteres no permitidos o demasiado corto: 400 que señala el campo documento")
    void registroConDocumentoInvalidoResponde400() {
        HttpResponse<String> respuesta = enviar("POST", "/api/auth/registro", null,
                registro(correoNuevo(), CONTRASENA_VALIDA, "12-3"));

        assertEquals(400, respuesta.statusCode(), respuesta.body());
        assertTrue(respuesta.body().contains("documento"), respuesta.body());
    }

    @Test
    @DisplayName("Registro sin campos obligatorios: 400")
    void registroSinCamposResponde400() {
        HttpResponse<String> respuesta = enviar("POST", "/api/auth/registro", null, "{}");

        assertEquals(400, respuesta.statusCode(), respuesta.body());
        assertTrue(respuesta.body().contains("VALIDACION"), respuesta.body());
    }

    // ---------------- 401 en la cadena de filtros ----------------

    @Test
    @DisplayName("Sin token: 401 con el ErrorResponse estándar y el encabezado WWW-Authenticate")
    void sinTokenResponde401ConErrorResponse() {
        HttpResponse<String> respuesta = enviar("GET", "/api/apartamentos", null, null);

        assertEquals(401, respuesta.statusCode(), respuesta.body());
        assertTrue(respuesta.body().contains("NO_AUTENTICADO"), "El 401 debe traer JSON, no cuerpo vacío: " + respuesta.body());
        assertTrue(respuesta.headers().firstValue("WWW-Authenticate").isPresent(), "Falta WWW-Authenticate");
    }

    @Test
    @DisplayName("Token que no es un JWT válido: 401 con el ErrorResponse estándar")
    void tokenInvalidoResponde401() {
        HttpResponse<String> respuesta = enviar("GET", "/api/apartamentos", "este-token-no-es-valido", null);

        assertEquals(401, respuesta.statusCode(), respuesta.body());
        assertTrue(respuesta.body().contains("NO_AUTENTICADO"), respuesta.body());
    }
}

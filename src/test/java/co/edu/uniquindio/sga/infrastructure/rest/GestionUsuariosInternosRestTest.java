package co.edu.uniquindio.sga.infrastructure.rest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Creación de cuentas internas y control de acceso exclusivo del administrador. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GestionUsuariosInternosRestTest extends PruebaRestBase {

    private String tokenAdmin;
    private String tokenRecepcion;
    private String tokenServicio;

    @BeforeAll
    void iniciarSesiones() {
        tokenAdmin = iniciarSesion("admin@sga.local", "Admin2026");
        tokenRecepcion = iniciarSesion("recepcion@sga.local", "Recepcion2026");
        tokenServicio = iniciarSesion("servicio@sga.local", "Servicio2026");
    }

    private String correoNuevo() {
        return "personal-" + UUID.randomUUID().toString().substring(0, 8) + "@prueba.local";
    }

    private String documentoNuevo() {
        return "8" + UUID.randomUUID().toString().replace("-", "").substring(0, 9);
    }

    private String solicitud(String correo, String documento, String rol) {
        return """
                {
                  "nombre": "Empleado de prueba",
                  "correo": "%s",
                  "contrasena": "Empleado2026",
                  "telefono": "3001234567",
                  "documento": "%s",
                  "fechaNacimiento": "1995-04-20",
                  "rol": "%s"
                }
                """.formatted(correo, documento, rol);
    }

    @Test
    @DisplayName("El administrador crea una cuenta de recepción que puede iniciar sesión con ese rol")
    void administradorCreaRecepcionista() {
        String correo = correoNuevo();
        String documento = documentoNuevo();

        var creada = enviar("POST", "/api/auth/usuarios-internos", tokenAdmin,
                solicitud(correo, documento, "RECEPCIONISTA"));

        assertEquals(201, creada.statusCode(), creada.body());
        assertTrue(creada.body().contains("RECEPCIONISTA"), creada.body());
        assertFalse(creada.body().contains("contrasena"), "La respuesta no debe exponer la contraseña");

        var login = enviar("POST", "/api/auth/login", null,
                credenciales(correo, "Empleado2026"));
        assertEquals(200, login.statusCode(), login.body());
        assertTrue(login.body().contains("RECEPCIONISTA"), login.body());
    }

    @Test
    @DisplayName("El administrador puede crear cuentas de personal de servicio y consultarlas")
    void administradorCreaPersonalDeServicio() {
        String correo = correoNuevo();
        var creada = enviar("POST", "/api/auth/usuarios-internos", tokenAdmin,
                solicitud(correo, documentoNuevo(), "PERSONAL_SERVICIO"));

        assertEquals(201, creada.statusCode(), creada.body());
        assertTrue(creada.body().contains("PERSONAL_SERVICIO"), creada.body());

        var listado = enviar("GET", "/api/auth/usuarios-internos", tokenAdmin, null);
        assertEquals(200, listado.statusCode(), listado.body());
        assertTrue(listado.body().contains(correo), listado.body());
    }

    @Test
    @DisplayName("Recepción y servicio no pueden crear ni consultar cuentas internas")
    void soloAdministradorGestionaCuentasInternas() {
        String cuerpo = solicitud(correoNuevo(), documentoNuevo(), "RECEPCIONISTA");
        assertProhibido(tokenRecepcion, "POST", "/api/auth/usuarios-internos", cuerpo);
        assertProhibido(tokenRecepcion, "GET", "/api/auth/usuarios-internos", null);
        assertProhibido(tokenServicio, "POST", "/api/auth/usuarios-internos", cuerpo);
        assertProhibido(tokenServicio, "GET", "/api/auth/usuarios-internos", null);
    }

    @Test
    @DisplayName("El administrador no puede crear cuentas de huésped ni de otro administrador")
    void soloPermiteRolesInternos() {
        var huesped = enviar("POST", "/api/auth/usuarios-internos", tokenAdmin,
                solicitud(correoNuevo(), documentoNuevo(), "HUESPED"));
        assertEquals(400, huesped.statusCode(), huesped.body());

        var administrador = enviar("POST", "/api/auth/usuarios-internos", tokenAdmin,
                solicitud(correoNuevo(), documentoNuevo(), "ADMINISTRADOR"));
        assertEquals(400, administrador.statusCode(), administrador.body());
    }
}

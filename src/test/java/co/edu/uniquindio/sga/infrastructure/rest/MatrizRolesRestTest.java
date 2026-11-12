package co.edu.uniquindio.sga.infrastructure.rest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Matriz de roles (7.1) comprobada contra la aplicación real, usando los usuarios iniciales (un usuario por rol y dos
 * huéspedes). En las pruebas 403 los cuerpos son válidos a propósito: Spring valida el cuerpo antes de aplicar
 * @PreAuthorize, así que con un cuerpo inválido la respuesta sería 400 y no se comprobaría el rol.
 * Las operaciones que modifican datos apuntan a un apartamento que no existe: si la seguridad fallara, el resultado
 * sería un 404 visible y no un daño a los datos de la Ficha.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MatrizRolesRestTest extends PruebaRestBase {

    private record Operacion(String metodo, String ruta, String cuerpo) {
    }

    private static final String APARTAMENTO = """
            {
              "identificacion": "ROL-001",
              "nombre": "Apartamento de la prueba de roles",
              "capacidad": 2,
              "dormitorios": [ { "descripcion": "Dormitorio 1", "tipoCama": "Cama doble" } ],
              "estadoOperativoInicial": "PREPARADO"
            }
            """;
    private static final String CAPACIDAD = "{ \"capacidad\": 4 }";
    private static final String BLOQUEO = """
            { "fechaInicio": "2027-05-01", "fechaFin": "2027-05-05", "motivo": "Mantenimiento programado" }
            """;
    private static final String TARIFA = """
            { "temporadaId": "00000000-0000-0000-0000-000000000000", "valorPorNoche": 90000, "vigenteDesde": "2027-01-01" }
            """;
    private static final String TEMPORADA = """
            { "nombre": "Temporada de la prueba de roles", "fechaInicio": "2028-06-01", "fechaFin": "2028-08-01" }
            """;
    private static final String POLITICA = """
            {
              "vigenteDesde": "2028-01-01",
              "tramos": [
                { "antelacionMinimaHoras": 240, "porcentajeRetencion": 0.00 },
                { "antelacionMinimaHoras": 0, "porcentajeRetencion": 1.00 }
              ]
            }
            """;
    private static final String PAGO = """
            { "medioPago": "TRANSFERENCIA", "valor": 1000, "referencia": "PRUEBA-ROLES" }
            """;
    private static final String CANCELACION = "{ \"motivo\": \"Cambio de planes del huésped\" }";
    private static final String CONFIRMACION = "{ \"medioPago\": \"TRANSFERENCIA\", \"referencia\": \"PRUEBA-ROLES\" }";

    private static final String RESERVA_INEXISTENTE = "/api/reservas/RES-2026-99999";
    private static final String UUID_NULO = "00000000-0000-0000-0000-000000000000";

    /** Solo el administrador configura el alojamiento, los apartamentos, los bloqueos y las tarifas. */
    private static final List<Operacion> SOLO_ADMINISTRADOR = List.of(
            new Operacion("POST", "/api/apartamentos", APARTAMENTO),
            new Operacion("PUT", "/api/apartamentos/NO-EXISTE/capacidad", CAPACIDAD),
            new Operacion("PUT", "/api/apartamentos/NO-EXISTE/retirar", null),
            new Operacion("POST", "/api/apartamentos/NO-EXISTE/bloquear", BLOQUEO),
            new Operacion("POST", "/api/apartamentos/NO-EXISTE/tarifas", TARIFA),
            new Operacion("POST", "/api/alojamiento/temporadas", TEMPORADA),
            new Operacion("POST", "/api/alojamiento/politica-cancelacion", POLITICA));

    /** Consultas de configuración: recepcionista y administrador. */
    private static final List<Operacion> CONSULTAS_DE_PERSONAL = List.of(
            new Operacion("GET", "/api/apartamentos/NO-EXISTE/tarifas", null),
            new Operacion("GET", "/api/apartamentos/NO-EXISTE/tarifas/" + UUID_NULO, null),
            new Operacion("GET", "/api/apartamentos/NO-EXISTE/bloqueos", null),
            new Operacion("GET", "/api/apartamentos/NO-EXISTE/bloqueos/" + UUID_NULO, null),
            new Operacion("GET", "/api/alojamiento/politica-cancelacion", null),
            new Operacion("GET", "/api/alojamiento/politica-cancelacion/1", null),
            new Operacion("GET", "/api/alojamiento/temporadas/" + UUID_NULO, null));

    private String tokenAdmin;
    private String tokenRecepcion;
    private String tokenServicio;
    private String tokenHuesped1;
    private String tokenHuesped2;

    @BeforeAll
    void iniciarSesiones() {
        tokenAdmin = iniciarSesion("admin@sga.local", "Admin2026");
        tokenRecepcion = iniciarSesion("recepcion@sga.local", "Recepcion2026");
        tokenServicio = iniciarSesion("servicio@sga.local", "Servicio2026");
        tokenHuesped1 = iniciarSesion("huesped1@sga.local", "Huesped2026");
        tokenHuesped2 = iniciarSesion("huesped2@sga.local", "Huesped2026");
    }

    private static String reserva(String apartamento, LocalDate entrada, String canal, String documento, String nombre) {
        return """
                {
                  "identificacionApartamento": "%s",
                  "fechaEntrada": "%s",
                  "fechaSalida": "%s",
                  "canalOrigen": "%s",
                  "titular": { "documento": "%s", "nombre": "%s", "fechaNacimiento": "1995-01-30" },
                  "ocupantes": []
                }
                """.formatted(apartamento, entrada, entrada.plusDays(2), canal, documento, nombre);
    }

    private static String cotizacion(LocalDate entrada) {
        return """
                {
                  "identificacionApartamento": "ATK-001",
                  "fechaEntrada": "%s",
                  "fechaSalida": "%s",
                  "ocupantes": [ { "documento": "1094000111", "nombre": "Laura Gomez", "fechaNacimiento": "1995-01-30" } ]
                }
                """.formatted(entrada, entrada.plusDays(2));
    }

    // ---------------- Huésped ----------------

    @Test
    @DisplayName("El huésped no puede configurar, consultar la configuración, listar el inventario ni operar reservas ajenas")
    void huespedNoTieneAccesoDePersonal() {
        for (Operacion o : SOLO_ADMINISTRADOR) {
            assertProhibido(tokenHuesped1, o.metodo(), o.ruta(), o.cuerpo());
        }
        for (Operacion o : CONSULTAS_DE_PERSONAL) {
            assertProhibido(tokenHuesped1, o.metodo(), o.ruta(), o.cuerpo());
        }
        assertProhibido(tokenHuesped1, "GET", "/api/apartamentos", null);                          // inventario completo
        assertProhibido(tokenHuesped1, "GET", "/api/reservas", null);                              // todas las reservas
        assertProhibido(tokenHuesped1, "PUT", RESERVA_INEXISTENTE + "/confirmar", CONFIRMACION);
        assertProhibido(tokenHuesped1, "PUT", "/api/apartamentos/NO-EXISTE/iniciar-preparacion", null);
        assertProhibido(tokenHuesped1, "POST", RESERVA_INEXISTENTE + "/folio/pagos", PAGO);
    }

    @Test
    @DisplayName("El huésped sí puede ver el catálogo, buscar disponibilidad, cotizar y reservar")
    void huespedPuedeUsarElPortal() {
        LocalDate entrada = LocalDate.now().plusDays(90);

        assertPermitido(tokenHuesped1, "GET", "/api/alojamiento", null);
        assertPermitido(tokenHuesped1, "GET", "/api/apartamentos/ATK-001", null);
        assertPermitido(tokenHuesped1, "GET", "/api/apartamentos/disponibles?fechaEntrada=" + entrada
                + "&fechaSalida=" + entrada.plusDays(2) + "&adultos=2&ninos=0", null);
        assertPermitido(tokenHuesped1, "POST", "/api/cotizaciones", cotizacion(entrada));
        assertPermitido(tokenHuesped1, "POST", "/api/reservas",
                reserva("NO-EXISTE", entrada, "PORTAL", "1094000111", "Laura Gomez"));   // 404: pasó la seguridad
    }

    // ---------------- Recepcionista ----------------

    @Test
    @DisplayName("La recepcionista no puede configurar el alojamiento ni mover el estado de preparación")
    void recepcionNoConfigura() {
        for (Operacion o : SOLO_ADMINISTRADOR) {
            assertProhibido(tokenRecepcion, o.metodo(), o.ruta(), o.cuerpo());
        }
        assertProhibido(tokenRecepcion, "PUT", "/api/apartamentos/NO-EXISTE/iniciar-preparacion", null);
        assertProhibido(tokenRecepcion, "PUT", "/api/apartamentos/NO-EXISTE/marcar-preparado", null);
    }

    @Test
    @DisplayName("La recepcionista sí consulta la configuración, el inventario, las reservas y el folio, y registra pagos")
    void recepcionOperaLaReceptoria() {
        for (Operacion o : CONSULTAS_DE_PERSONAL) {
            assertPermitido(tokenRecepcion, o.metodo(), o.ruta(), o.cuerpo());
        }
        assertPermitido(tokenRecepcion, "GET", "/api/apartamentos", null);
        assertPermitido(tokenRecepcion, "GET", "/api/reservas", null);
        assertPermitido(tokenRecepcion, "GET", RESERVA_INEXISTENTE + "/folio", null);
        assertPermitido(tokenRecepcion, "POST", RESERVA_INEXISTENTE + "/folio/pagos", PAGO);
        assertPermitido(tokenRecepcion, "POST", "/api/cotizaciones", cotizacion(LocalDate.now().plusDays(90)));
    }

    // ---------------- Personal de servicio ----------------

    @Test
    @DisplayName("El personal de servicio solo consulta el inventario y prepara apartamentos")
    void servicioSoloPreparaApartamentos() {
        LocalDate entrada = LocalDate.now().plusDays(90);

        assertProhibido(tokenServicio, "POST", "/api/reservas", reserva("NO-EXISTE", entrada, "DIRECTO", "1000000400", "Titular"));
        assertProhibido(tokenServicio, "GET", "/api/reservas", null);
        assertProhibido(tokenServicio, "GET", RESERVA_INEXISTENTE + "/folio", null);
        assertProhibido(tokenServicio, "POST", RESERVA_INEXISTENTE + "/folio/pagos", PAGO);
        assertProhibido(tokenServicio, "POST", "/api/cotizaciones", cotizacion(entrada));
        for (Operacion o : SOLO_ADMINISTRADOR) {
            assertProhibido(tokenServicio, o.metodo(), o.ruta(), o.cuerpo());
        }
        for (Operacion o : CONSULTAS_DE_PERSONAL) {
            assertProhibido(tokenServicio, o.metodo(), o.ruta(), o.cuerpo());
        }

        assertPermitido(tokenServicio, "GET", "/api/apartamentos", null);
        assertPermitido(tokenServicio, "PUT", "/api/apartamentos/NO-EXISTE/iniciar-preparacion", null);   // 404: pasó la seguridad
        assertPermitido(tokenServicio, "PUT", "/api/apartamentos/NO-EXISTE/marcar-preparado", null);
    }

    // ---------------- Administrador ----------------

    @Test
    @DisplayName("El administrador accede a la configuración, a las consultas de personal y a la preparación de apartamentos")
    void administradorAccedeATodo() {
        for (Operacion o : CONSULTAS_DE_PERSONAL) {
            assertPermitido(tokenAdmin, o.metodo(), o.ruta(), o.cuerpo());
        }
        assertPermitido(tokenAdmin, "GET", "/api/apartamentos", null);
        assertPermitido(tokenAdmin, "GET", "/api/reservas", null);
        assertPermitido(tokenAdmin, "PUT", "/api/apartamentos/NO-EXISTE/retirar", null);               // 404: pasó la seguridad
        assertPermitido(tokenAdmin, "POST", "/api/apartamentos/NO-EXISTE/bloquear", BLOQUEO);
        assertPermitido(tokenAdmin, "PUT", "/api/apartamentos/NO-EXISTE/iniciar-preparacion", null);
    }

    // ---------------- Propietario de la reserva (7.2) ----------------

    @Test
    @DisplayName("Un huésped solo ve, cancela y consulta el folio de SU reserva; la recepción ve cualquiera")
    void elHuespedSoloAccedeASuPropiaReserva() {
        LocalDate entrada = LocalDate.now().plusDays(60);

        // El huésped 1 (documento 1094000111, el mismo que lleva su token) reserva por el portal como titular
        HttpResponse<String> creada = enviar("POST", "/api/reservas", tokenHuesped1,
                reserva("CTL-001", entrada, "PORTAL", "1094000111", "Laura Gomez"));
        assertEquals(201, creada.statusCode(), creada.body());
        String ruta = "/api/reservas/" + codigoDeReserva(creada.body());

        // El titular accede a su reserva y a su folio
        assertEquals(200, enviar("GET", ruta, tokenHuesped1, null).statusCode());
        assertEquals(200, enviar("GET", ruta + "/folio", tokenHuesped1, null).statusCode());

        // Otro huésped tiene el mismo rol, pero la reserva no es suya
        assertProhibido(tokenHuesped2, "GET", ruta, null);
        assertProhibido(tokenHuesped2, "GET", ruta + "/folio", null);
        assertProhibido(tokenHuesped2, "PUT", ruta + "/cancelar", CANCELACION);

        // La recepción accede a cualquier reserva
        assertEquals(200, enviar("GET", ruta, tokenRecepcion, null).statusCode());

        // El titular puede cancelar la suya
        HttpResponse<String> cancelada = enviar("PUT", ruta + "/cancelar", tokenHuesped1, CANCELACION);
        assertEquals(200, cancelada.statusCode(), cancelada.body());
    }
}

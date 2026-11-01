package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.Cancelacion;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DetalleNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaAnticipo;
import co.edu.uniquindio.sga.domain.valueobject.RecargoLlegadaNocturna;
import co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntradaSalidaNoShowTest {

    private static final LocalDateTime CREADA = LocalDateTime.of(2026, 10, 30, 10, 0);
    private static final LocalDate ENTRADA = LocalDate.of(2026, 12, 1);
    private static final HoraLimiteNoShow LIMITE = new HoraLimiteNoShow(LocalTime.of(21, 0));

    private PoliticaCancelacion politica;
    private Reserva reserva;

    @BeforeEach
    void preparar() {
        politica = PoliticaCancelacion.crear(1, LocalDate.of(2026, 1, 1), List.of(
                new TramoAntelacion(168, BigDecimal.ZERO), new TramoAntelacion(72, new BigDecimal("0.50")),
                new TramoAntelacion(0, BigDecimal.ONE)));
        // 2 noches x 100.000 x 1 ocupante = 200.000; anticipo del 50% = 100.000
        reserva = new Reserva(new CodigoReserva("RES-2026-00001"), "ATK-001",
                Titular.crear("1000000001", "Carlos Pérez", LocalDate.of(1990, 5, 15)),
                new Estancia(ENTRADA, ENTRADA.plusDays(2)), CanalOrigen.PORTAL,
                new Cotizacion(List.of(new DetalleNoche(ENTRADA, "Base", Dinero.de(100_000), 1),
                        new DetalleNoche(ENTRADA.plusDays(1), "Base", Dinero.de(100_000), 1))),
                new VersionPolitica(1, LocalDate.of(2026, 1, 1)), CREADA);
        reserva.agregarOcupante(Ocupante.crear("1000000001", "Carlos Pérez", LocalDate.of(1990, 5, 15)));
        reserva.registrarHoraEstimadaLlegada(LocalTime.of(15, 0),
                new RecargoLlegadaNocturna(Dinero.de(30_000), LocalTime.of(22, 0), LocalTime.of(5, 0)));
    }

    private void confirmar() {
        reserva.confirmarConAnticipo(new PoliticaAnticipo(true, new BigDecimal("0.50")), new PlazoConfirmacion(24),
                MedioPago.TRANSFERENCIA, "REF-1", CREADA.plusHours(1));
    }

    // ---------------- Pagos (RN-15) ----------------

    @Test
    @DisplayName("Pagos: RN-15 se registra un pago contra el folio y el saldo baja")
    void registraUnPagoYBajaElSaldo() {
        confirmar();

        reserva.registrarPago(Dinero.de(60_000), MedioPago.EFECTIVO, null, LocalDate.of(2026, 12, 1));

        assertEquals(Dinero.de(160_000), reserva.getFolio().calcularTotalPagos());
        assertEquals(Dinero.de(40_000), reserva.getFolio().calcularSaldoPendiente());
    }

    @Test
    @DisplayName("Pagos: RN-15 no se puede pagar más que el saldo pendiente")
    void rechazaPagarDeMas() {
        confirmar();

        assertThrows(ReglaDominioException.class,
                () -> reserva.registrarPago(Dinero.de(100_001), MedioPago.EFECTIVO, null, LocalDate.of(2026, 12, 1)));
        assertEquals(Dinero.de(100_000), reserva.getFolio().calcularTotalPagos());
    }

    @Test
    @DisplayName("Pagos: RN-15 un pago debe ser mayor a cero")
    void rechazaPagoEnCero() {
        confirmar();

        assertThrows(ReglaDominioException.class,
                () -> reserva.registrarPago(Dinero.cero(), MedioPago.EFECTIVO, null, LocalDate.of(2026, 12, 1)));
    }

    @Test
    @DisplayName("Pagos: no se registran pagos en una reserva PENDIENTE (el anticipo se paga al confirmar)")
    void rechazaPagoEnReservaPendiente() {
        assertThrows(ReglaDominioException.class,
                () -> reserva.registrarPago(Dinero.de(10_000), MedioPago.EFECTIVO, null, LocalDate.of(2026, 12, 1)));
    }

    @Test
    @DisplayName("Pagos: no se registran pagos en una reserva ya cancelada")
    void rechazaPagoEnReservaCancelada() {
        confirmar();
        reserva.cancelar(politica, "Cambio de planes", CREADA.plusDays(1));

        assertThrows(ReglaDominioException.class,
                () -> reserva.registrarPago(Dinero.de(10_000), MedioPago.EFECTIVO, null, LocalDate.of(2026, 12, 1)));
    }

    // ---------------- Check-out (RN-17) ----------------

    @Test
    @DisplayName("Salida: RN-17 no se hace el check-out con saldo pendiente")
    void rechazaSalidaConSaldoPendiente() {
        confirmar();
        reserva.realizarCheckIn();

        assertThrows(ReglaDominioException.class, reserva::realizarCheckOut);
        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
    }

    @Test
    @DisplayName("Salida: RN-17 con el folio en paz y salvo la reserva pasa a FINALIZADA")
    void permiteSalidaConElFolioPagado() {
        confirmar();
        reserva.realizarCheckIn();
        reserva.registrarPago(Dinero.de(100_000), MedioPago.EFECTIVO, null, LocalDate.of(2026, 12, 3));

        reserva.realizarCheckOut();

        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
        assertFalse(reserva.getEstado().esActiva());
    }

    @Test
    @DisplayName("Salida: el recargo nocturno también debe estar pago para poder salir")
    void elRecargoNocturnoTambienSeDebePagar() {
        Reserva nocturna = new Reserva(new CodigoReserva("RES-2026-00002"), "ATK-001",
                Titular.crear("1000000002", "Llegada Tardía", LocalDate.of(1990, 5, 15)),
                new Estancia(ENTRADA, ENTRADA.plusDays(1)), CanalOrigen.PORTAL,
                new Cotizacion(List.of(new DetalleNoche(ENTRADA, "Base", Dinero.de(100_000), 1))),
                new VersionPolitica(1, LocalDate.of(2026, 1, 1)), CREADA);
        nocturna.agregarOcupante(Ocupante.crear("1000000002", "Llegada Tardía", LocalDate.of(1990, 5, 15)));
        nocturna.registrarHoraEstimadaLlegada(LocalTime.of(23, 0),
                new RecargoLlegadaNocturna(Dinero.de(30_000), LocalTime.of(22, 0), LocalTime.of(5, 0)));
        nocturna.confirmarConAnticipo(new PoliticaAnticipo(true, new BigDecimal("0.50")), new PlazoConfirmacion(24),
                MedioPago.TARJETA, "T-1", CREADA.plusHours(1));
        nocturna.realizarCheckIn();
        // 100.000 de hospedaje + 30.000 de recargo = 130.000; anticipo 50.000
        nocturna.registrarPago(Dinero.de(50_000), MedioPago.EFECTIVO, null, LocalDate.of(2026, 12, 2));

        assertThrows(ReglaDominioException.class, nocturna::realizarCheckOut); // faltan 30.000

        nocturna.registrarPago(Dinero.de(30_000), MedioPago.EFECTIVO, null, LocalDate.of(2026, 12, 2));
        nocturna.realizarCheckOut();
        assertEquals(EstadoReserva.FINALIZADA, nocturna.getEstado());
    }

    // ---------------- No-show (RN-12, RN-13) ----------------

    @Test
    @DisplayName("No-show: RN-12 después de las 9 pm del día de entrada se declara y se retiene el 100% del anticipo")
    void declaraNoShowYRetieneTodo() {
        confirmar();

        Cancelacion cancelacion = reserva.declararNoShow(LIMITE, politica, ENTRADA.atTime(21, 5));

        assertEquals(EstadoReserva.NO_SHOW, reserva.getEstado());
        assertFalse(reserva.getEstado().esActiva()); // RN-12: libera las noches
        assertEquals(Dinero.de(100_000), cancelacion.retenido());
        assertEquals(Dinero.cero(), cancelacion.devuelto());
        assertTrue(cancelacion.motivo().startsWith("NO_SHOW"));
    }

    @Test
    @DisplayName("No-show: RN-12 antes de la hora límite todavía no se puede declarar")
    void rechazaNoShowAntesDeLaHoraLimite() {
        confirmar();

        assertThrows(ReglaDominioException.class, () -> reserva.declararNoShow(LIMITE, politica, ENTRADA.atTime(20, 59)));
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    @DisplayName("No-show: un día después de la entrada también corresponde")
    void declaraNoShowDiasDespues() {
        confirmar();

        assertTrue(reserva.haPasadoHoraLimiteNoShow(LIMITE, ENTRADA.plusDays(1).atTime(8, 0)));
        reserva.declararNoShow(LIMITE, politica, ENTRADA.plusDays(1).atTime(8, 0));

        assertEquals(EstadoReserva.NO_SHOW, reserva.getEstado());
    }

    @Test
    @DisplayName("No-show: RN-08 solo una reserva CONFIRMADA puede ser no-show")
    void soloConfirmadaPuedeSerNoShow() {
        assertFalse(reserva.haPasadoHoraLimiteNoShow(LIMITE, ENTRADA.atTime(22, 0))); // PENDIENTE
        assertThrows(ReglaDominioException.class, () -> reserva.declararNoShow(LIMITE, politica, ENTRADA.atTime(22, 0)));

        confirmar();
        reserva.realizarCheckIn();
        assertFalse(reserva.haPasadoHoraLimiteNoShow(LIMITE, ENTRADA.atTime(22, 0))); // EN_CURSO: ya llegó
    }

    @Test
    @DisplayName("No-show: RN-13 usa la versión de la política congelada en la reserva")
    void noShowExigeLaVersionCongelada() {
        confirmar();
        PoliticaCancelacion versionDos = PoliticaCancelacion.crear(2, LocalDate.of(2026, 11, 1), List.of(
                new TramoAntelacion(24, BigDecimal.ZERO), new TramoAntelacion(0, BigDecimal.ONE)));

        assertThrows(ReglaDominioException.class, () -> reserva.declararNoShow(LIMITE, versionDos, ENTRADA.atTime(22, 0)));
    }

    // ---------------- Estado operativo del apartamento (7.6) ----------------

    @Test
    @DisplayName("Apartamento: el ciclo completo PREPARADO -> OCUPADO -> PENDIENTE_PREPARACION -> EN_PREPARACION -> PREPARADO")
    void cicloOperativoCompleto() {
        Apartamento apartamento = Apartamento.crear("ATK-001", "Attack Suite", 4,
                List.of(new Dormitorio("Dormitorio 1", "Cama doble")), EstadoOperativo.PREPARADO);

        apartamento.registrarOcupacion();
        assertEquals(EstadoOperativo.OCUPADO, apartamento.estadoOperativo());
        apartamento.registrarDesocupacion();
        assertEquals(EstadoOperativo.PENDIENTE_PREPARACION, apartamento.estadoOperativo());
        apartamento.iniciarPreparacion();
        assertEquals(EstadoOperativo.EN_PREPARACION, apartamento.estadoOperativo());
        apartamento.marcarPreparado();
        assertEquals(EstadoOperativo.PREPARADO, apartamento.estadoOperativo());
    }

    @Test
    @DisplayName("Apartamento: las transiciones fuera de orden se rechazan")
    void rechazaTransicionesFueraDeOrden() {
        Apartamento apartamento = Apartamento.crear("ATK-001", "Attack Suite", 4,
                List.of(new Dormitorio("Dormitorio 1", "Cama doble")), EstadoOperativo.PREPARADO);

        assertThrows(ReglaDominioException.class, apartamento::registrarDesocupacion);
        assertThrows(ReglaDominioException.class, apartamento::marcarPreparado);
        apartamento.registrarOcupacion();
        assertThrows(ReglaDominioException.class, apartamento::registrarOcupacion);
        assertThrows(ReglaDominioException.class, apartamento::sacarDeServicio); // ocupado: primero debe salir el grupo
    }
}

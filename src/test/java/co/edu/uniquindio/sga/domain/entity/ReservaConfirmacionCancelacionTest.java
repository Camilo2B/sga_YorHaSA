package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.Cancelacion;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DetalleNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservaConfirmacionCancelacionTest {

    private static final LocalDateTime CREADA = LocalDateTime.of(2026, 10, 30, 10, 0);
    private static final LocalDate ENTRADA = LocalDate.of(2026, 12, 1);
    private static final PlazoConfirmacion PLAZO = new PlazoConfirmacion(24);
    private static final PoliticaAnticipo ANTICIPO_50 = new PoliticaAnticipo(true, new BigDecimal("0.50"));
    private static final RecargoLlegadaNocturna RECARGO = new RecargoLlegadaNocturna(Dinero.de(30_000), LocalTime.of(22, 0), LocalTime.of(5, 0));

    private PoliticaCancelacion politica;
    private Reserva reserva;

    @BeforeEach
    void preparar() {
        // Ficha: más de 7 días no retiene, entre 3 y 7 días retiene 50%, menos de 3 días retiene 100%
        politica = PoliticaCancelacion.crear(1, LocalDate.of(2026, 1, 1), List.of(
                new TramoAntelacion(168, BigDecimal.ZERO), new TramoAntelacion(72, new BigDecimal("0.50")),
                new TramoAntelacion(0, BigDecimal.ONE)));
        // 2 noches x 100.000 x 1 ocupante facturable = 200.000; anticipo del 50% = 100.000
        Cotizacion cotizacion = new Cotizacion(List.of(
                new DetalleNoche(ENTRADA, "Base", Dinero.de(100_000), 1),
                new DetalleNoche(ENTRADA.plusDays(1), "Base", Dinero.de(100_000), 1)));
        reserva = new Reserva(new co.edu.uniquindio.sga.domain.valueobject.CodigoReserva("RES-2026-00001"), "ATK-001",
                Titular.crear("1000000001", "Carlos Pérez", LocalDate.of(1990, 5, 15)),
                new Estancia(ENTRADA, ENTRADA.plusDays(2)), CanalOrigen.PORTAL, cotizacion,
                new VersionPolitica(1, LocalDate.of(2026, 1, 1)), CREADA);
        reserva.agregarOcupante(Ocupante.crear("1000000001", "Carlos Pérez", LocalDate.of(1990, 5, 15)));
    }

    private void registrarHoraDiurna() {
        reserva.registrarHoraEstimadaLlegada(LocalTime.of(15, 0), RECARGO);
    }

    private Reserva reservaConfirmada() {
        registrarHoraDiurna();
        reserva.confirmarConAnticipo(ANTICIPO_50, PLAZO, MedioPago.TRANSFERENCIA, "REF-1", CREADA.plusHours(2));
        return reserva;
    }

    // ---------------- Confirmar ----------------

    @Test
    @DisplayName("Confirmar: cobra el anticipo del 50% sobre el valor de la estancia y pasa a CONFIRMADA")
    void confirmaCobrandoElAnticipo() {
        registrarHoraDiurna();

        Pago pago = reserva.confirmarConAnticipo(ANTICIPO_50, PLAZO, MedioPago.TRANSFERENCIA, "REF-1", CREADA.plusHours(2));

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(Dinero.de(100_000), pago.valor());
        assertEquals(MedioPago.TRANSFERENCIA, pago.medio());
        assertEquals(CREADA.plusHours(2).toLocalDate(), pago.fecha());
        assertEquals(Dinero.de(100_000), reserva.getFolio().calcularTotalPagos());
        assertEquals(Dinero.de(100_000), reserva.getFolio().calcularSaldoPendiente());
    }

    @Test
    @DisplayName("Confirmar: RN-09 sin hora estimada de llegada no se confirma y no cambia nada")
    void rechazaConfirmarSinHoraEstimada() {
        assertThrows(ReglaDominioException.class,
                () -> reserva.confirmarConAnticipo(ANTICIPO_50, PLAZO, MedioPago.TRANSFERENCIA, "REF-1", CREADA.plusHours(2)));

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertTrue(reserva.getFolio().getPagos().isEmpty());
    }

    @Test
    @DisplayName("Confirmar: RN-21 pasado el plazo de 24 horas ya no se puede confirmar")
    void rechazaConfirmarFueraDePlazo() {
        registrarHoraDiurna();

        assertThrows(ReglaDominioException.class,
                () -> reserva.confirmarConAnticipo(ANTICIPO_50, PLAZO, MedioPago.TRANSFERENCIA, "REF-1", CREADA.plusHours(25)));

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertTrue(reserva.getFolio().getPagos().isEmpty());
    }

    @Test
    @DisplayName("Confirmar: RN-21 justo a las 24 horas todavía se puede confirmar")
    void aceptaConfirmarEnElLimiteDelPlazo() {
        registrarHoraDiurna();

        reserva.confirmarConAnticipo(ANTICIPO_50, PLAZO, MedioPago.EFECTIVO, null, CREADA.plusHours(24));

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    @DisplayName("Confirmar: RN-08 una reserva ya confirmada no se confirma de nuevo")
    void rechazaConfirmarDosVeces() {
        reservaConfirmada();

        assertThrows(ReglaDominioException.class,
                () -> reserva.confirmarConAnticipo(ANTICIPO_50, PLAZO, MedioPago.TRANSFERENCIA, "REF-2", CREADA.plusHours(3)));
        assertEquals(1, reserva.getFolio().getPagos().size());
    }

    @Test
    @DisplayName("Confirmar: si se exige anticipo hay que indicar el medio de pago")
    void exigeMedioDePagoParaElAnticipo() {
        registrarHoraDiurna();

        assertThrows(ReglaDominioException.class,
                () -> reserva.confirmarConAnticipo(ANTICIPO_50, PLAZO, null, null, CREADA.plusHours(2)));
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    @DisplayName("Confirmar: si el alojamiento no exige anticipo se confirma sin pago")
    void confirmaSinAnticipoSiNoSeExige() {
        registrarHoraDiurna();

        Pago pago = reserva.confirmarConAnticipo(PoliticaAnticipo.noExige(), PLAZO, null, null, CREADA.plusHours(2));

        assertNull(pago);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertTrue(reserva.getFolio().getPagos().isEmpty());
    }

    // ---------------- Cancelar ----------------

    @Test
    @DisplayName("Cancelar: RN-13 con más de 7 días de antelación se devuelve el 100% del anticipo")
    void cancelarConMasDeSieteDiasDevuelveTodo() {
        reservaConfirmada();

        Cancelacion cancelacion = reserva.cancelar(politica, "Cambio de planes", LocalDateTime.of(2026, 11, 10, 9, 0));

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertEquals(Dinero.cero(), cancelacion.retenido());
        assertEquals(Dinero.de(100_000), cancelacion.devuelto());
        assertEquals("Cambio de planes", cancelacion.motivo());
        assertEquals(cancelacion, reserva.getCancelacion().orElseThrow());
    }

    @Test
    @DisplayName("Cancelar: RN-13 entre 3 y 7 días retiene el 50% del anticipo")
    void cancelarEntreTresYSieteDiasRetieneLaMitad() {
        reservaConfirmada();

        Cancelacion cancelacion = reserva.cancelar(politica, "Cambio de planes", LocalDateTime.of(2026, 11, 27, 10, 0));

        assertEquals(Dinero.de(50_000), cancelacion.retenido());
        assertEquals(Dinero.de(50_000), cancelacion.devuelto());
    }

    @Test
    @DisplayName("Cancelar: RN-13 con menos de 3 días retiene el 100% del anticipo")
    void cancelarConMenosDeTresDiasRetieneTodo() {
        reservaConfirmada();

        Cancelacion cancelacion = reserva.cancelar(politica, "Cambio de planes", LocalDateTime.of(2026, 11, 30, 10, 0));

        assertEquals(Dinero.de(100_000), cancelacion.retenido());
        assertEquals(Dinero.cero(), cancelacion.devuelto());
    }

    @Test
    @DisplayName("Cancelar: una reserva PENDIENTE sin anticipo se cancela sin retención ni devolución")
    void cancelarPendienteSinPagos() {
        Cancelacion cancelacion = reserva.cancelar(politica, "No voy a viajar", LocalDateTime.of(2026, 11, 30, 10, 0));

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertEquals(Dinero.cero(), cancelacion.retenido());
        assertEquals(Dinero.cero(), cancelacion.devuelto());
    }

    @Test
    @DisplayName("Cancelar: la retención y la devolución siempre suman lo que se pagó")
    void retenidoMasDevueltoEsLoPagado() {
        reservaConfirmada();

        Cancelacion cancelacion = reserva.cancelar(politica, "Cambio de planes", LocalDateTime.of(2026, 11, 27, 10, 0));

        assertEquals(reserva.getFolio().calcularTotalPagos(), cancelacion.retenido().sumar(cancelacion.devuelto()));
    }

    @Test
    @DisplayName("Cancelar: RN-08 no se cancela una reserva EN_CURSO")
    void rechazaCancelarEnCurso() {
        reservaConfirmada();
        reserva.realizarCheckIn();

        assertThrows(ReglaDominioException.class, () -> reserva.cancelar(politica, "Muy tarde", LocalDateTime.of(2026, 12, 2, 10, 0)));
        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
    }

    @Test
    @DisplayName("Cancelar: RN-08 una reserva ya cancelada no se cancela de nuevo")
    void rechazaCancelarDosVeces() {
        reserva.cancelar(politica, "Primera vez", LocalDateTime.of(2026, 11, 10, 9, 0));

        assertThrows(ReglaDominioException.class, () -> reserva.cancelar(politica, "Segunda vez", LocalDateTime.of(2026, 11, 10, 9, 5)));
    }

    @Test
    @DisplayName("Cancelar: el motivo es obligatorio")
    void exigeMotivo() {
        assertThrows(ReglaDominioException.class, () -> reserva.cancelar(politica, "  ", LocalDateTime.of(2026, 11, 10, 9, 0)));
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    @DisplayName("Cancelar: RN-13 solo vale la versión de la política congelada en la reserva, no otra más reciente")
    void exigeLaVersionCongelada() {
        PoliticaCancelacion versionDos = PoliticaCancelacion.crear(2, LocalDate.of(2026, 11, 1), List.of(
                new TramoAntelacion(24, BigDecimal.ZERO), new TramoAntelacion(0, BigDecimal.ONE)));

        assertThrows(ReglaDominioException.class, () -> reserva.cancelar(versionDos, "Cambio de planes", LocalDateTime.of(2026, 11, 10, 9, 0)));
    }

    // ---------------- Vencimiento (RN-21) ----------------

    @Test
    @DisplayName("Vencimiento: RN-21 una reserva PENDIENTE fuera de plazo se cancela y libera sus noches")
    void cancelaPorVencimiento() {
        assertTrue(reserva.haVencidoPlazoConfirmacion(PLAZO, CREADA.plusHours(25)));

        Cancelacion cancelacion = reserva.cancelarPorVencimiento(PLAZO, CREADA.plusHours(25));

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertFalse(reserva.getEstado().esActiva());
        assertEquals(Dinero.cero(), cancelacion.retenido());
    }

    @Test
    @DisplayName("Vencimiento: RN-21 dentro del plazo, o con la reserva ya confirmada, no vence")
    void noVenceDentroDelPlazoNiConfirmada() {
        assertFalse(reserva.haVencidoPlazoConfirmacion(PLAZO, CREADA.plusHours(24)));
        assertThrows(ReglaDominioException.class, () -> reserva.cancelarPorVencimiento(PLAZO, CREADA.plusHours(24)));

        reservaConfirmada();

        assertFalse(reserva.haVencidoPlazoConfirmacion(PLAZO, CREADA.plusHours(100)));
    }
}

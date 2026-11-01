package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Titular;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DetalleNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaAnticipo;
import co.edu.uniquindio.sga.domain.valueobject.RecargoLlegadaNocturna;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntregaApartamentoServiceTest {

    private static final LocalDate ENTRADA = LocalDate.of(2026, 12, 1);
    private static final LocalDateTime CREADA = LocalDateTime.of(2026, 10, 30, 10, 0);

    private final EntregaApartamentoService servicio = new EntregaApartamentoService();
    private Apartamento apartamento;
    private Reserva reserva;

    @BeforeEach
    void preparar() {
        apartamento = Apartamento.crear("ATK-001", "Attack Suite", 4,
                List.of(new Dormitorio("Dormitorio 1", "Cama doble")), EstadoOperativo.PREPARADO);
        reserva = new Reserva(new CodigoReserva("RES-2026-00001"), "ATK-001",
                Titular.crear("1000000001", "Carlos Pérez", LocalDate.of(1990, 5, 15)),
                new Estancia(ENTRADA, ENTRADA.plusDays(2)), CanalOrigen.PORTAL,
                new Cotizacion(List.of(new DetalleNoche(ENTRADA, "Base", Dinero.de(100_000), 1),
                        new DetalleNoche(ENTRADA.plusDays(1), "Base", Dinero.de(100_000), 1))),
                new VersionPolitica(1, LocalDate.of(2026, 1, 1)), CREADA);
        reserva.agregarOcupante(Ocupante.crear("1000000001", "Carlos Pérez", LocalDate.of(1990, 5, 15)));
        reserva.registrarHoraEstimadaLlegada(LocalTime.of(15, 0),
                new RecargoLlegadaNocturna(Dinero.de(30_000), LocalTime.of(22, 0), LocalTime.of(5, 0)));
        reserva.confirmarConAnticipo(new PoliticaAnticipo(true, new java.math.BigDecimal("0.50")), new PlazoConfirmacion(24),
                MedioPago.TRANSFERENCIA, "REF-1", CREADA.plusHours(1));
    }

    @Test
    @DisplayName("Entrega: se autoriza con la reserva CONFIRMADA, en la fecha de entrada y el apartamento PREPARADO")
    void autorizaLaEntregaNormal() {
        assertDoesNotThrow(() -> servicio.autorizarEntrega(reserva, apartamento, ENTRADA));
    }

    @Test
    @DisplayName("Entrega: RN-10 se puede registrar en cualquier noche de la estancia, no solo la primera")
    void autorizaDuranteLaEstancia() {
        assertDoesNotThrow(() -> servicio.autorizarEntrega(reserva, apartamento, ENTRADA.plusDays(1)));
    }

    @Test
    @DisplayName("Entrega: RN-10 no se permite el registro antes de la fecha de entrada")
    void rechazaAntesDeLaEntrada() {
        assertThrows(ReglaDominioException.class, () -> servicio.autorizarEntrega(reserva, apartamento, ENTRADA.minusDays(1)));
    }

    @Test
    @DisplayName("Entrega: RN-10 no se registra la llegada cuando la estancia ya terminó")
    void rechazaDespuesDeLaSalida() {
        assertThrows(ReglaDominioException.class, () -> servicio.autorizarEntrega(reserva, apartamento, ENTRADA.plusDays(2)));
    }

    @Test
    @DisplayName("Entrega: RN-10 la reserva debe estar CONFIRMADA")
    void rechazaReservaNoConfirmada() {
        reserva.cancelar(co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion.crear(1, LocalDate.of(2026, 1, 1), List.of(
                new co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion(0, java.math.BigDecimal.ONE),
                new co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion(24, java.math.BigDecimal.ZERO))),
                "Cancelada para la prueba", CREADA.plusDays(1));

        assertThrows(ReglaDominioException.class, () -> servicio.autorizarEntrega(reserva, apartamento, ENTRADA));
    }

    @Test
    @DisplayName("Entrega: RN-11 el apartamento debe estar PREPARADO")
    void rechazaApartamentoNoPreparado() {
        apartamento.registrarOcupacion();

        assertThrows(ReglaDominioException.class, () -> servicio.autorizarEntrega(reserva, apartamento, ENTRADA));
    }

    @Test
    @DisplayName("Entrega: la reserva debe ser de ese apartamento")
    void rechazaApartamentoDeOtraReserva() {
        Apartamento otro = Apartamento.crear("ATK-002", "Otro", 4,
                List.of(new Dormitorio("Dormitorio 1", "Cama doble")), EstadoOperativo.PREPARADO);

        assertThrows(ReglaDominioException.class, () -> servicio.autorizarEntrega(reserva, otro, ENTRADA));
    }
}

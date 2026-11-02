package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {

    private Reserva crearReservaEjemplo() {
        String apartamentoId = "APT-101";
        Titular titular = Titular.crear("12345678", "Carlos Perez", LocalDate.of(1990, 5, 15));
        Estancia estancia = new Estancia(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5));

        Cotizacion cotizacion = new Cotizacion(
                List.of(new DetalleNoche(
                        LocalDate.of(2026, 12, 1),
                        "Temporada Alta",
                        Dinero.de(new BigDecimal("100000")),
                        2
                ))
        );

        return new Reserva(new CodigoReserva("RES-2026-00001"), apartamentoId, titular, estancia, CanalOrigen.DIRECTO, cotizacion,
                new VersionPolitica(1, LocalDate.of(2026, 1, 1)), LocalDateTime.of(2026, 10, 1, 9, 0));
    }

    @Test
    @DisplayName("Prueba 7 (Entidad): No se puede realizar Check-In si la reserva no esta CONFIRMADA")
    void lanzarExcepcionEnCheckInSinConfirmar() {
        Reserva reserva = crearReservaEjemplo();
        reserva.agregarOcupante(Ocupante.crear("87654321", "Juan Gomez", LocalDate.of(1995, 8, 20)));
        assertThrows(ReglaDominioException.class, reserva::realizarCheckIn);
    }

    @Test
    @DisplayName("Prueba 8 (Entidad): No se puede realizar Check-Out si la reserva tiene saldo pendiente")
    void lanzarExcepcionEnCheckOutConSaldoPendiente() {
        Reserva reserva = crearReservaEjemplo();
        reserva.confirmar();
        reserva.agregarOcupante(Ocupante.crear("87654321", "Juan Gomez", LocalDate.of(1995, 8, 20)));
        reserva.realizarCheckIn();
        assertThrows(ReglaDominioException.class, reserva::realizarCheckOut);
    }

    @Test
    @DisplayName("No se puede confirmar una reserva que no este PENDIENTE")
    void noPermiteConfirmarSiNoPendiente() {
        Reserva reserva = crearReservaEjemplo();
        reserva.confirmar();
        assertThrows(ReglaDominioException.class, reserva::confirmar);
    }

    @Test
    @DisplayName("No se puede hacer check-in sin ocupantes")
    void noPermiteCheckInSinOcupantes() {
        Reserva reserva = crearReservaEjemplo();
        reserva.confirmar();
        assertThrows(ReglaDominioException.class, reserva::realizarCheckIn);
    }
}

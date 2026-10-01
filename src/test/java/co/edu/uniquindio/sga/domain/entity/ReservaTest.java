package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {

    private Reserva crearReservaEjemplo() {
        UUID apartamentoId = UUID.randomUUID();
        Titular titular = Titular.crear("12345678", "Carlos Pérez", LocalDate.of(1990, 5, 15));
        Estancia estancia = new Estancia(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5));

        Cotizacion cotizacion = new Cotizacion(
                List.of(new DetalleNoche(
                        LocalDate.of(2026, 12, 1),
                        "Temporada Alta",
                        Dinero.de(new BigDecimal("100000")),
                        2
                ))
        );

        return new Reserva(apartamentoId, titular, estancia, CanalOrigen.DIRECTO, cotizacion);
    }

    @Test
    @DisplayName("Prueba 7 (Entidad): No se puede realizar Check-In si la reserva no está CONFIRMADA")
    void lanzarExcepcionEnCheckInSinConfirmar() {
        // Arrange
        Reserva reserva = crearReservaEjemplo(); // Inicia PENDIENTE
        reserva.agregarOcupante(Ocupante.crear("87654321", "Juan Gómez", LocalDate.of(1995, 8, 20)));

        // Act & Assert
        assertThrows(ReglaDominioException.class, reserva::realizarCheckIn);
    }

    @Test
    @DisplayName("Prueba 8 (Entidad): No se puede realizar Check-Out si la reserva tiene saldo pendiente")
    void lanzarExcepcionEnCheckOutConSaldoPendiente() {
        // Arrange
        Reserva reserva = crearReservaEjemplo();
        reserva.confirmar();
        reserva.agregarOcupante(Ocupante.crear("87654321", "Juan Gómez", LocalDate.of(1995, 8, 20)));
        reserva.realizarCheckIn();

        // Act & Assert
        assertThrows(ReglaDominioException.class, reserva::realizarCheckOut);
    }
}
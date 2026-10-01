package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Titular;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.*;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.InMemoryApartamentoRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.InMemoryReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GestionBloqueoServiceTest {

    private ApartamentoRepository apartamentoRepository;
    private ReservaRepository reservaRepository;
    private GestionBloqueoService servicio;

    @BeforeEach
    void setUp() {
        apartamentoRepository = new InMemoryApartamentoRepository();
        reservaRepository = new InMemoryReservaRepository();
        servicio = new GestionBloqueoService(apartamentoRepository, reservaRepository);
    }

    @Test
    @DisplayName("Prueba 11 (Servicio): Registrar bloqueo exitoso cuando no hay reservas solapadas")
    void registrarBloqueoExitosamente() {
        // Arrange
        Apartamento apt = Apartamento.crear("APT-101", "Apt Deluxe", 4,
                List.of(new Dormitorio("Principal", "King")), EstadoOperativo.PREPARADO);
        apartamentoRepository.guardar(apt);

        Bloqueo bloqueo = Bloqueo.crear(
                new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 5)),
                "Mantenimiento de pintura"
        );

        // Act
        servicio.registrarBloqueo("APT-101", bloqueo);

        // Assert
        Apartamento aptGuardado = apartamentoRepository.buscarPorId("APT-101").orElseThrow();
        assertEquals(1, aptGuardado.bloqueos().size());
    }

    @Test
    @DisplayName("Prueba 12 (Servicio): Lanzar excepción si se intenta bloquear en fechas con reserva activa (Regla 7.3)")
    void lanzarExcepcionAlBloquearFechaConReservaActiva() {
        // Arrange
        Apartamento apt = Apartamento.crear(
                "APT-101",
                "Apt Deluxe",
                4,
                List.of(new Dormitorio("Principal", "King")),
                EstadoOperativo.PREPARADO
        );
        apartamentoRepository.guardar(apt);

        Estancia estanciaReserva = new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 5));

        Cotizacion cotizacion = new Cotizacion(
                List.of(new DetalleNoche(
                        LocalDate.of(2026, 11, 1),
                        "Temporada Alta",
                        Dinero.de(100000),
                        2
                ))
        );

        Reserva reserva = new Reserva(
                UUID.nameUUIDFromBytes("APT-101".getBytes()),
                Titular.crear("123", "Ana", LocalDate.of(1995, 4, 12)),
                estanciaReserva,
                CanalOrigen.DIRECTO,
                cotizacion
        );
        reserva.confirmar();
        reservaRepository.guardar(reserva);

        Bloqueo bloqueo = Bloqueo.crear(
                new Estancia(LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 7)),
                "Mantenimiento correctivo"
        );

        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> servicio.registrarBloqueo("APT-101", bloqueo));
    }
}
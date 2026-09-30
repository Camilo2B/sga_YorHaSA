package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Titular;
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

class DisponibilidadApartamentoServiceTest {

    private ApartamentoRepository apartamentoRepository;
    private ReservaRepository reservaRepository;
    private DisponibilidadApartamentoService servicio;

    @BeforeEach
    void setUp() {
        apartamentoRepository = new InMemoryApartamentoRepository();
        reservaRepository = new InMemoryReservaRepository();
        servicio = new DisponibilidadApartamentoService(apartamentoRepository, reservaRepository);
    }

    @Test
    @DisplayName("Prueba 9 (Servicio): Apartamento no está disponible si supera la capacidad de huéspedes")
    void noDisponibleSiExcedeCapacidad() {
        // Arrange
        Apartamento apt = Apartamento.crear("APT-101", "Apt Deluxe", 2,
                List.of(new Dormitorio("Principal", "Doble")), EstadoOperativo.PREPARADO);
        apartamentoRepository.guardar(apt);

        Estancia estancia = new Estancia(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15));

        // Act (Solicita 4 personas para capacidad de 2)
        boolean disponible = servicio.estaDisponible("APT-101", estancia, 4);

        // Assert
        assertFalse(disponible);
    }

    @Test
    @DisplayName("Prueba 10 (Servicio): Apartamento no está disponible si hay una reserva activa solapada")
    void noDisponibleSiExisteReservaSolapada() {
        // Arrange
        Apartamento apt = Apartamento.crear(
                "APT-101",
                "Apt Deluxe",
                4,
                List.of(new Dormitorio("Principal", "Doble")),
                EstadoOperativo.PREPARADO
        );
        apartamentoRepository.guardar(apt);

        Estancia estanciaExistente = new Estancia(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15));

        Cotizacion cotizacion = new Cotizacion(
                List.of(new DetalleNoche(
                        LocalDate.of(2026, 10, 10),
                        "Temporada Alta",
                        Dinero.de(100000),
                        2
                ))
        );

        Reserva reserva = new Reserva(
                UUID.nameUUIDFromBytes("APT-101".getBytes()),
                Titular.crear("123", "Ana", LocalDate.of(1995, 4, 12)),
                estanciaExistente,
                CanalOrigen.DIRECTO,
                cotizacion
        );
        reserva.confirmar();
        reservaRepository.guardar(reserva);

        Estancia estanciaSolicitada = new Estancia(LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 18));

        // Act
        boolean disponible = servicio.estaDisponible("APT-101", estanciaSolicitada, 2);

        // Assert
        assertFalse(disponible);
    }
}
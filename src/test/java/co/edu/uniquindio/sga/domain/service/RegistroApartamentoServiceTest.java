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
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.ApartamentoRepositoryInMemory;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.ReservaRepositoryInMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroApartamentoServiceTest {

    private ApartamentoRepositoryInMemory apartamentoRepository;
    private ReservaRepositoryInMemory reservaRepository;
    private RegistroApartamentoService servicio;

    @BeforeEach
    void preparar() {
        apartamentoRepository = new ApartamentoRepositoryInMemory();
        reservaRepository = new ReservaRepositoryInMemory();
        servicio = new RegistroApartamentoService(apartamentoRepository, reservaRepository);
    }

    private Apartamento apartamento(String identificacion) {
        return Apartamento.crear(identificacion, "Attack Suite", 4,
                List.of(new Dormitorio("Dormitorio principal", "Cama doble")), EstadoOperativo.PREPARADO);
    }

    /** Reserva PENDIENTE (activa) con el titular y un acompañante: ocupa 2 cupos. */
    private Reserva reservaDeDos(String apartamentoId) {
        LocalDate entrada = LocalDate.of(2026, 12, 1);
        Reserva reserva = new Reserva(new CodigoReserva("RES-2026-00001"), apartamentoId,
                Titular.crear("123", "Ana", LocalDate.of(1995, 4, 12)), new Estancia(entrada, entrada.plusDays(2)),
                CanalOrigen.PORTAL, new Cotizacion(List.of(new DetalleNoche(entrada, "Base", Dinero.de(80_000), 2))),
                new VersionPolitica(1, LocalDate.of(2026, 1, 1)), LocalDateTime.of(2026, 10, 30, 9, 0));
        reserva.agregarOcupante(Titular.crear("123", "Ana", LocalDate.of(1995, 4, 12)));
        reserva.agregarOcupante(Ocupante.crear("456", "Luis", LocalDate.of(1993, 2, 1)));
        reservaRepository.guardar(reserva);
        return reserva;
    }

    @Test
    @DisplayName("Prueba 13 (Servicio): registra un apartamento con identificación nueva")
    void registraApartamentoNuevo() {
        // Arrange
        Apartamento nuevo = apartamento("ATK-001");

        // Act
        servicio.registrar(nuevo);

        // Assert
        assertTrue(apartamentoRepository.buscarPorId("ATK-001").isPresent());
    }

    @Test
    @DisplayName("Prueba 14 (Servicio): L-04 rechaza una identificación repetida y no pisa el original")
    void rechazaIdentificacionRepetida() {
        // Arrange
        Apartamento original = apartamento("ATK-001");
        servicio.registrar(original);

        // Act + Assert
        assertThrows(ReglaDominioException.class, () -> servicio.registrar(apartamento("ATK-001")));
        assertEquals(1, apartamentoRepository.obtenerTodos().size());
    }

    @Test
    @DisplayName("Inventario: 7.3 un apartamento sin reservas activas se retira de la venta (eliminación lógica)")
    void retiraApartamentoSinReservasActivas() {
        Apartamento apartamento = apartamento("ATK-001");

        servicio.retirar(apartamento);

        assertFalse(apartamento.activo());
        assertThrows(ReglaDominioException.class, () -> servicio.retirar(apartamento));
    }

    @Test
    @DisplayName("Inventario: 7.3 no se retira un apartamento con reservas activas o futuras")
    void noRetiraApartamentoConReservasActivas() {
        Apartamento apartamento = apartamento("ATK-001");
        reservaDeDos("ATK-001");

        assertThrows(ReglaDominioException.class, () -> servicio.retirar(apartamento));
        assertTrue(apartamento.activo());
    }

    @Test
    @DisplayName("Inventario: RN-02 la capacidad no puede bajar de los cupos que ya ocupa una reserva activa")
    void cambiaCapacidadRespetandoLasReservasActivas() {
        Apartamento apartamento = apartamento("ATK-001");
        reservaDeDos("ATK-001");

        assertThrows(ReglaDominioException.class, () -> servicio.cambiarCapacidad(apartamento, 1));
        assertEquals(4, apartamento.capacidad());

        servicio.cambiarCapacidad(apartamento, 2);
        assertEquals(2, apartamento.capacidad());
    }
}

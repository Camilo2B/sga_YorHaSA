package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Titular;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.*;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.ApartamentoRepositoryInMemory;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.ReservaRepositoryInMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DisponibilidadApartamentoServiceTest {

    private ApartamentoRepository apartamentoRepository;
    private ReservaRepository reservaRepository;
    private DisponibilidadApartamentoService servicio;

    /** Ficha: salida 11:00 y entrada 15:00 dejan 4 horas; con 3 de preparación cabe el recambio el mismo día. */
    private static final ParametrosAlojamiento PARAMETROS = parametros(3);

    @BeforeEach
    void setUp() {
        apartamentoRepository = new ApartamentoRepositoryInMemory();
        reservaRepository = new ReservaRepositoryInMemory();
        servicio = new DisponibilidadApartamentoService(apartamentoRepository, reservaRepository);
    }

    private static ParametrosAlojamiento parametros(int horasPreparacion) {
        return new ParametrosAlojamiento(new UmbralEdadFacturable(12),
                new HorarioAlojamiento(LocalTime.of(15, 0), LocalTime.of(11, 0)),
                new TiempoPreparacion(horasPreparacion), new PlazoConfirmacion(24),
                new HoraLimiteNoShow(LocalTime.of(21, 0)), new PoliticaAnticipo(true, new BigDecimal("0.5")));
    }

    private Apartamento apartamento(String id, int capacidad, boolean permiteMascotas) {
        Apartamento apt = Apartamento.crear(id, "Apt " + id, capacidad,
                List.of(new Dormitorio("Principal", "Doble")), null, permiteMascotas, EstadoOperativo.PREPARADO);
        apartamentoRepository.guardar(apt);
        return apt;
    }

    private void reservaConfirmada(String apartamentoId, LocalDate entrada, LocalDate salida) {
        Cotizacion cotizacion = new Cotizacion(List.of(new DetalleNoche(entrada, "Temporada Alta", Dinero.de(100000), 2)));
        Reserva reserva = new Reserva(
                new CodigoReserva("RES-2026-0000" + (reservaRepository.buscar(null, null, null, null).size() + 1)),
                apartamentoId,
                Titular.crear("123", "Ana", LocalDate.of(1995, 4, 12)),
                new Estancia(entrada, salida),
                CanalOrigen.DIRECTO,
                cotizacion,
                new VersionPolitica(1, LocalDate.of(2026, 1, 1)),
                LocalDateTime.of(2026, 10, 1, 9, 0)
        );
        reserva.confirmar();
        reservaRepository.guardar(reserva);
    }

    @Test
    @DisplayName("Prueba 9 (Servicio): Apartamento no está disponible si supera la capacidad de huéspedes")
    void noDisponibleSiExcedeCapacidad() {
        Apartamento apt = apartamento("APT-101", 2, false);
        Estancia estancia = new Estancia(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15));

        // Solicita 4 personas para capacidad de 2
        assertFalse(servicio.estaDisponible(apt, estancia, 4, false, PARAMETROS));
    }

    @Test
    @DisplayName("Prueba 10 (Servicio): Apartamento no está disponible si hay una reserva activa solapada")
    void noDisponibleSiExisteReservaSolapada() {
        Apartamento apt = apartamento("APT-101", 4, false);
        reservaConfirmada("APT-101", LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15));

        Estancia estanciaSolicitada = new Estancia(LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 18));

        assertFalse(servicio.estaDisponible(apt, estanciaSolicitada, 2, false, PARAMETROS));
    }

    @Test
    @DisplayName("Disponibilidad: RN-20 si el tiempo de preparación cabe, una estancia puede empezar el día de salida de otra")
    void recambioMismoDiaCuandoAlcanzaLaPreparacion() {
        Apartamento apt = apartamento("APT-101", 4, false);
        reservaConfirmada("APT-101", LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15));

        assertTrue(servicio.estaDisponible(apt, new Estancia(LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 17)),
                2, false, PARAMETROS));
    }

    @Test
    @DisplayName("Disponibilidad: RN-20 si la preparación no cabe entre salida y entrada, no hay recambio el mismo día")
    void sinRecambioMismoDiaCuandoNoAlcanzaLaPreparacion() {
        Apartamento apt = apartamento("APT-101", 4, false);
        reservaConfirmada("APT-101", LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15));
        ParametrosAlojamiento preparacionLarga = parametros(5);

        assertFalse(servicio.estaDisponible(apt, new Estancia(LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 17)),
                2, false, preparacionLarga));
        assertFalse(servicio.estaDisponible(apt, new Estancia(LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 10)),
                2, false, preparacionLarga));
        assertTrue(servicio.estaDisponible(apt, new Estancia(LocalDate.of(2026, 10, 16), LocalDate.of(2026, 10, 18)),
                2, false, preparacionLarga));
    }

    @Test
    @DisplayName("Disponibilidad: RN-Propia-03 con mascota solo sirven los apartamentos que la permiten y con un cupo libre")
    void conMascotaSoloApartamentosQueLaPermiten() {
        apartamento("APT-101", 4, false);
        apartamento("APT-102", 4, true);
        apartamento("APT-103", 3, true);
        Estancia estancia = new Estancia(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 12));

        List<Apartamento> disponibles = servicio.buscarDisponibles(estancia, 3, true, PARAMETROS);

        // APT-101 no permite mascotas; APT-103 tiene 3 cupos y 3 personas + mascota necesitan 4
        assertEquals(List.of("APT-102"), disponibles.stream().map(Apartamento::identificacion).toList());
    }

    @Test
    @DisplayName("Disponibilidad: la búsqueda descarta los apartamentos retirados, ocupados en esas noches o sin capacidad")
    void buscarDisponiblesDescartaLosNoAptos() {
        apartamento("APT-101", 4, false);
        apartamento("APT-102", 2, false);
        Apartamento retirado = apartamento("APT-103", 4, false);
        retirado.desactivar();
        apartamento("APT-104", 4, false);
        reservaConfirmada("APT-104", LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 11));
        Estancia estancia = new Estancia(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 12));

        List<Apartamento> disponibles = servicio.buscarDisponibles(estancia, 3, false, PARAMETROS);

        assertEquals(List.of("APT-101"), disponibles.stream().map(Apartamento::identificacion).toList());
    }
}

package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Titular;
import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.Cancelacion;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DetalleNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.Gravedad;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaAnticipo;
import co.edu.uniquindio.sga.domain.valueobject.RecargoLlegadaNocturna;
import co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba la persistencia contra H2 con el contexto completo. Cada prueba hace flush y clear antes de leer, para que
 * la reserva se reconstruya desde la base de datos y no desde la caché de la sesión. Se revierte al terminar.
 */
@SpringBootTest
@Transactional
class ReservaJpaRepositoryTest {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private GeneradorCodigoReserva generadorCodigo;

    @Autowired
    private EntityManager entityManager;

    private Reserva nuevaReserva(String apartamentoId, LocalDate entrada, LocalDate salida) {
        return nuevaReserva(apartamentoId, entrada, salida, false);
    }

    private Reserva nuevaReserva(String apartamentoId, LocalDate entrada, LocalDate salida, boolean conMascota) {
        return nuevaReserva(apartamentoId, entrada, salida, conMascota, LocalDateTime.of(2026, 10, 30, 9, 30));
    }

    private Reserva nuevaReserva(String apartamentoId, LocalDate entrada, LocalDate salida, boolean conMascota,
                                 LocalDateTime fechaCreacion) {
        Cotizacion cotizacion = new Cotizacion(List.of(
                new DetalleNoche(entrada, "Media (Sincronización)", Dinero.de(100_000), 2),
                new DetalleNoche(entrada.plusDays(1), "Alta (Despliegue de Misión)", Dinero.de(125_000), 2)));
        return new Reserva(generadorCodigo.generar(), apartamentoId,
                Titular.crear("1094123456", "Carlos Pérez", LocalDate.of(1990, 5, 15)),
                new Estancia(entrada, salida), CanalOrigen.PORTAL, cotizacion,
                new VersionPolitica(1, LocalDate.of(2026, 1, 1)), fechaCreacion, conMascota);
    }

    /**
     * Guarda la reserva y fuerza el INSERT de inmediato. El generador cuenta las reservas ya guardadas, así que cada
     * reserva debe guardarse antes de crear la siguiente (en la aplicación lo hace el caso de uso en una sola transacción).
     */
    private Reserva guardada(Reserva reserva) {
        reservaRepository.guardar(reserva);
        entityManager.flush();
        return reserva;
    }

    /** La versión 1 de la política que las reservas de estas pruebas tienen congelada. */
    private static PoliticaCancelacion politicaV1() {
        return PoliticaCancelacion.crear(1, LocalDate.of(2026, 1, 1), List.of(
                new TramoAntelacion(168, BigDecimal.ZERO), new TramoAntelacion(72, new BigDecimal("0.50")),
                new TramoAntelacion(0, BigDecimal.ONE)));
    }

    private void guardarYLimpiar(Reserva... reservas) {
        for (Reserva reserva : reservas) {
            reservaRepository.guardar(reserva);
        }
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Persistencia: la reserva completa (titular, cotización, folio, ocupantes y novedades) se reconstruye igual")
    void guardaYReconstruyeLaReservaCompleta() {
        Reserva original = nuevaReserva("ATK-001", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3), true);
        original.registrarHoraEstimadaLlegada(LocalTime.of(23, 0),
                new RecargoLlegadaNocturna(Dinero.de(30_000), LocalTime.of(22, 0), LocalTime.of(5, 0)));
        original.agregarOcupante(Ocupante.crear("87654321", "Juan Gómez", LocalDate.of(1995, 8, 20)));
        original.getFolio().registrarPago(Dinero.de(225_000), MedioPago.TRANSFERENCIA, "REF-1");
        original.registrarNovedad("Operario", "Fuga en el baño", Gravedad.MEDIA);
        original.confirmar();
        guardarYLimpiar(original);

        Reserva leida = reservaRepository.obtenerPorCodigo(original.getCodigo()).orElseThrow();

        assertEquals(original.getId(), leida.getId());
        assertEquals(original.getCodigo(), leida.getCodigo());
        assertEquals("ATK-001", leida.getApartamentoId());
        assertEquals(EstadoReserva.CONFIRMADA, leida.getEstado());
        assertEquals(CanalOrigen.PORTAL, leida.getCanalOrigen());
        assertEquals("1094123456", leida.getTitular().documento().numero()); // <-- Corregido aquí con .numero()
        assertEquals(LocalDate.of(1990, 5, 15), leida.getTitular().fechaNacimiento());
        assertEquals(original.getEstancia(), leida.getEstancia());
        assertEquals(new VersionPolitica(1, LocalDate.of(2026, 1, 1)), leida.getVersionPolitica());
        assertEquals(LocalDateTime.of(2026, 10, 30, 9, 30), leida.getFechaCreacion());
        // La cotización queda congelada, noche por noche y en orden de fecha (RN-22).
        assertEquals(2, leida.getCotizacion().totalNoches());
        assertEquals(LocalDate.of(2026, 12, 1), leida.getCotizacion().desglosePorNoche().get(0).fecha());
        assertEquals(Dinero.de(450_000), leida.getCotizacion().valorTotal());
        // El folio conserva su identidad, sus cargos de hospedaje y el pago.
        assertEquals(original.getFolio().getId(), leida.getFolio().getId());
        assertTrue(leida.isConMascota());
        assertEquals(LocalTime.of(23, 0), leida.getHoraEstimadaLlegada());
        assertEquals(3, leida.getFolio().getCargos().size()); // 2 noches + recargo por llegada nocturna
        assertEquals(Dinero.de(480_000), leida.getFolio().calcularTotalCargos());
        assertEquals(Dinero.de(225_000), leida.getFolio().calcularTotalPagos());
        assertEquals(Dinero.de(255_000), leida.getFolio().calcularSaldoPendiente());
        assertEquals(1, leida.getOcupantes().size());
        assertEquals("Juan Gómez", leida.getOcupantes().get(0).nombre());
        assertEquals(1, leida.getNovedades().size());
        assertEquals(Gravedad.MEDIA, leida.getNovedades().get(0).gravedad());
    }

    @Test
    @DisplayName("Persistencia: una reserva guardada de nuevo actualiza su estado sin duplicarse")
    void guardarDeNuevoActualiza() {
        // La base ya trae las reservas de prueba del arranque: se comparan los conteos antes y después.
        int confirmadasAntes = reservaRepository.buscarPorEstado(EstadoReserva.CONFIRMADA).size();
        int pendientesAntes = reservaRepository.buscarPorEstado(EstadoReserva.PENDIENTE).size();
        Reserva reserva = nuevaReserva("ATK-001", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3));
        guardarYLimpiar(reserva);

        Reserva leida = reservaRepository.obtenerPorCodigo(reserva.getCodigo()).orElseThrow();
        leida.confirmar();
        guardarYLimpiar(leida);

        assertEquals(EstadoReserva.CONFIRMADA, reservaRepository.obtenerPorCodigo(reserva.getCodigo()).orElseThrow().getEstado());
        assertEquals(confirmadasAntes + 1, reservaRepository.buscarPorEstado(EstadoReserva.CONFIRMADA).size());
        assertEquals(pendientesAntes, reservaRepository.buscarPorEstado(EstadoReserva.PENDIENTE).size());
    }

    @Test
    @DisplayName("Persistencia: buscarActivasPorApartamento sin fechas trae las activas del apartamento (vigentes y futuras)")
    void buscaActivasDelApartamentoEnCualquierFecha() {
        Reserva futura = guardada(nuevaReserva("HLR-002", LocalDate.of(2027, 3, 1), LocalDate.of(2027, 3, 3)));
        Reserva cancelada = nuevaReserva("HLR-002", LocalDate.of(2026, 12, 2), LocalDate.of(2026, 12, 4));
        cancelada.cancelar(politicaV1(), "Cancelada en la prueba", LocalDateTime.of(2026, 10, 30, 10, 0));
        guardada(cancelada);
        guardada(nuevaReserva("HLR-001", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5)));
        entityManager.clear();

        List<Reserva> activas = reservaRepository.buscarActivasPorApartamento("HLR-002");

        assertEquals(List.of(futura.getCodigo()), activas.stream().map(Reserva::getCodigo).toList());
    }

    @Test
    @DisplayName("Persistencia: 7.5 el listado filtra por estado, apartamento y fechas, de la más reciente a la más antigua")
    void listadoFiltradoYOrdenado() {
        Reserva antigua = guardada(nuevaReserva("EXT-001", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 3), false,
                LocalDateTime.of(2026, 10, 1, 8, 0)));
        Reserva reciente = guardada(nuevaReserva("EXT-001", LocalDate.of(2027, 2, 10), LocalDate.of(2027, 2, 12), false,
                LocalDateTime.of(2026, 10, 20, 8, 0)));
        Reserva confirmada = nuevaReserva("EXT-001", LocalDate.of(2027, 3, 1), LocalDate.of(2027, 3, 3), false,
                LocalDateTime.of(2026, 10, 10, 8, 0));
        confirmada.confirmar();
        guardada(confirmada);
        entityManager.clear();

        List<CodigoReserva> delApartamento = reservaRepository.buscar(null, "EXT-001", null, null).stream()
                .map(Reserva::getCodigo).toList();
        List<CodigoReserva> pendientes = reservaRepository.buscar(EstadoReserva.PENDIENTE, "EXT-001", null, null).stream()
                .map(Reserva::getCodigo).toList();
        // Solo la antigua tiene noches en [2027-02-02, 2027-02-10): la reciente entra justo el día en que termina el rango
        List<CodigoReserva> enFebrero = reservaRepository.buscar(null, "EXT-001", LocalDate.of(2027, 2, 2),
                LocalDate.of(2027, 2, 10)).stream().map(Reserva::getCodigo).toList();

        assertEquals(List.of(reciente.getCodigo(), confirmada.getCodigo(), antigua.getCodigo()), delApartamento);
        assertEquals(List.of(reciente.getCodigo(), antigua.getCodigo()), pendientes);
        assertEquals(List.of(antigua.getCodigo()), enFebrero);
    }

    @Test
    @DisplayName("Persistencia: la cancelación (motivo, fecha y reparto del anticipo) se guarda y se reconstruye")
    void guardaYReconstruyeLaCancelacion() {
        Reserva reserva = nuevaReserva("ATK-001", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3));
        reserva.registrarHoraEstimadaLlegada(LocalTime.of(15, 0),
                new RecargoLlegadaNocturna(Dinero.de(30_000), LocalTime.of(22, 0), LocalTime.of(5, 0)));
        reserva.confirmarConAnticipo(new PoliticaAnticipo(true, new BigDecimal("0.50")), new PlazoConfirmacion(24),
                MedioPago.TARJETA, "REF-9", LocalDateTime.of(2026, 10, 30, 10, 0));
        // 2 noches (100.000 + 125.000) x 2 = 450.000 de cotización; anticipo 225.000; 31 días de antelación: retención 0%
        reserva.cancelar(politicaV1(), "El huésped cambió de planes", LocalDateTime.of(2026, 10, 30, 11, 0));
        guardarYLimpiar(reserva);

        Reserva leida = reservaRepository.obtenerPorCodigo(reserva.getCodigo()).orElseThrow();

        assertEquals(EstadoReserva.CANCELADA, leida.getEstado());
        Cancelacion cancelacion = leida.getCancelacion().orElseThrow();
        assertEquals("El huésped cambió de planes", cancelacion.motivo());
        assertEquals(LocalDateTime.of(2026, 10, 30, 11, 0), cancelacion.fecha());
        assertEquals(Dinero.cero(), cancelacion.retenido());
        assertEquals(Dinero.de(225_000), cancelacion.devuelto());
        assertEquals(Dinero.de(225_000), leida.getFolio().calcularTotalPagos());
    }

    @Test
    @DisplayName("Persistencia: buscarActivasPorApartamento solo trae las activas del apartamento que se solapan (3.1)")
    void buscaSoloActivasSolapadasDelApartamento() {
        Reserva solapada = guardada(nuevaReserva("ATK-001", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5)));
        guardada(nuevaReserva("ATK-001", LocalDate.of(2026, 12, 8), LocalDate.of(2026, 12, 10)));
        Reserva cancelada = nuevaReserva("ATK-001", LocalDate.of(2026, 12, 2), LocalDate.of(2026, 12, 4));
        cancelada.cancelar(politicaV1(), "Cancelada en la prueba", LocalDateTime.of(2026, 10, 30, 10, 0));
        guardada(cancelada);
        guardada(nuevaReserva("ATK-002", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5)));
        entityManager.clear();

        List<Reserva> encontradas = reservaRepository.buscarActivasPorApartamento(
                "ATK-001", new Estancia(LocalDate.of(2026, 12, 3), LocalDate.of(2026, 12, 8)));

        assertEquals(1, encontradas.size());
        assertEquals(solapada.getCodigo(), encontradas.get(0).getCodigo());
    }

    @Test
    @DisplayName("Persistencia: una estancia que empieza el día de salida de otra no se solapa (intervalo [entrada, salida))")
    void elDiaDeSalidaNoSeSolapa() {
        guardarYLimpiar(nuevaReserva("ATK-001", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5)));

        List<Reserva> encontradas = reservaRepository.buscarActivasPorApartamento(
                "ATK-001", new Estancia(LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 7)));

        assertTrue(encontradas.isEmpty());
    }

    @Test
    @DisplayName("Persistencia: el generador entrega códigos consecutivos con el formato RES-AAAA-NNNNN")
    void generaCodigosConsecutivos() {
        CodigoReserva primero = generadorCodigo.generar();
        Reserva reserva = new Reserva(primero, "ATK-001",
                Titular.crear("1094123456", "Carlos Pérez", LocalDate.of(1990, 5, 15)),
                new Estancia(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 2)), CanalOrigen.DIRECTO,
                new Cotizacion(List.of(new DetalleNoche(LocalDate.of(2026, 12, 1), "Base", Dinero.de(80_000), 1))),
                new VersionPolitica(1, LocalDate.of(2026, 1, 1)), LocalDateTime.of(2026, 10, 30, 9, 30));
        guardarYLimpiar(reserva);

        CodigoReserva segundo = generadorCodigo.generar();

        assertNotEquals(primero, segundo);
        assertEquals(Long.parseLong(primero.valor().substring(9)) + 1, Long.parseLong(segundo.valor().substring(9)));
    }
}
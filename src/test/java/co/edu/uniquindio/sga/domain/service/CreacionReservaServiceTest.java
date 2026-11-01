package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.entity.Titular;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.DescuentoEstanciaProlongada;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.valueobject.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaAnticipo;
import co.edu.uniquindio.sga.domain.valueobject.RecargoLlegadaNocturna;
import co.edu.uniquindio.sga.domain.valueobject.SolicitudReserva;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.ApartamentoRepositoryInMemory;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.GeneradorCodigoReservaInMemory;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.ReservaRepositoryInMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreacionReservaServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 30, 10, 0);
    private static final LocalDate DESDE_SIEMPRE = LocalDate.of(2026, 1, 1);

    private ReservaRepositoryInMemory reservaRepository;
    private CreacionReservaService servicio;
    private Alojamiento alojamiento;
    private Apartamento apartamento;
    private Titular titular;

    @BeforeEach
    void preparar() {
        ApartamentoRepositoryInMemory apartamentoRepository = new ApartamentoRepositoryInMemory();
        reservaRepository = new ReservaRepositoryInMemory();

        ParametrosAlojamiento parametros = new ParametrosAlojamiento(new UmbralEdadFacturable(12),
                new HorarioAlojamiento(LocalTime.of(15, 0), LocalTime.of(11, 0)),
                new TiempoPreparacion(3), new PlazoConfirmacion(24),
                new HoraLimiteNoShow(LocalTime.of(21, 0)), new PoliticaAnticipo(true, new BigDecimal("0.5")));
        alojamiento = Alojamiento.crear("Búnker Orbital YoRHa", "El Espacio", parametros, Temporada.crearBase("Base"),
                List.of(new TramoAntelacion(168, BigDecimal.ZERO), new TramoAntelacion(0, BigDecimal.ONE)),
                LocalDate.of(2026, 10, 1));
        Temporada base = alojamiento.temporadas().stream().filter(Temporada::esBase).findFirst().orElseThrow();

        apartamento = Apartamento.crear("ATK-001", "Attack Suite", 4,
                List.of(new Dormitorio("Dormitorio 1", "Cama doble")), EstadoOperativo.PREPARADO);
        apartamento.definirTarifa(base.id(), Dinero.de(80_000), DESDE_SIEMPRE);
        apartamentoRepository.guardar(apartamento);

        servicio = new CreacionReservaService(
                new DisponibilidadApartamentoService(apartamentoRepository, reservaRepository),
                new CotizadorEstanciaService(new DescuentoEstanciaProlongada(5, new BigDecimal("0.10"))),
                new GeneradorCodigoReservaInMemory(2026),
                new RecargoLlegadaNocturna(Dinero.de(30_000), LocalTime.of(22, 0), LocalTime.of(5, 0)));

        titular = Titular.crear("1000000001", "Carlos Pérez", LocalDate.of(1990, 5, 15));
    }

    private static Ocupante adulto(String documento) {
        return Ocupante.crear(documento, "Adulto " + documento, LocalDate.of(1992, 8, 20));
    }

    private SolicitudReserva solicitud(LocalDate entrada, LocalDate salida, List<Ocupante> acompanantes,
                                       boolean conMascota, LocalTime hora) {
        return new SolicitudReserva(titular, acompanantes, new Estancia(entrada, salida), CanalOrigen.PORTAL, conMascota, hora);
    }

    private SolicitudReserva solicitudBasica() {
        return solicitud(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3), List.of(adulto("1000000002")), false, null);
    }

    @Test
    @DisplayName("Creación: la reserva nace PENDIENTE con código, valor y política congelados, y el titular cuenta como ocupante")
    void creaReservaPendienteConValorYPoliticaCongelados() {
        Reserva reserva = servicio.crear(alojamiento, apartamento, solicitudBasica(), AHORA);

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals("RES-2026-00001", reserva.getCodigo().valor());
        assertEquals(2, reserva.getOcupantes().size());
        assertEquals(1, reserva.getVersionPolitica().numero());
        assertEquals(AHORA, reserva.getFechaCreacion());
        // 2 noches x 80.000 x 2 ocupantes facturables
        assertEquals(Dinero.de(320_000), reserva.getCotizacion().valorTotal());
        assertEquals(Dinero.de(320_000), reserva.getFolio().calcularTotalCargos());
        assertNull(reserva.getHoraEstimadaLlegada());
    }

    @Test
    @DisplayName("Creación: RN-04 no se crean reservas con entrada anterior a hoy")
    void rechazaEntradaEnElPasado() {
        SolicitudReserva enElPasado = solicitud(LocalDate.of(2026, 10, 29), LocalDate.of(2026, 10, 31), List.of(), false, null);

        assertThrows(ReglaDominioException.class, () -> servicio.crear(alojamiento, apartamento, enElPasado, AHORA));
    }

    @Test
    @DisplayName("Creación: RN-04 una entrada hoy mismo sí es válida")
    void aceptaEntradaHoy() {
        SolicitudReserva hoy = solicitud(LocalDate.of(2026, 10, 30), LocalDate.of(2026, 10, 31), List.of(), false, null);

        assertEquals(EstadoReserva.PENDIENTE, servicio.crear(alojamiento, apartamento, hoy, AHORA).getEstado());
    }

    @Test
    @DisplayName("Creación: 3.2 el titular no puede ser menor del umbral de edad facturable")
    void rechazaTitularMenor() {
        titular = Titular.crear("1000000001", "Menor", LocalDate.of(2019, 3, 10));

        assertThrows(ReglaDominioException.class, () -> servicio.crear(alojamiento, apartamento, solicitudBasica(), AHORA));
    }

    @Test
    @DisplayName("Creación: un ocupante no puede repetirse (mismo documento que el titular)")
    void rechazaDocumentoRepetido() {
        SolicitudReserva repetido = solicitud(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3),
                List.of(adulto("1000000001")), false, null);

        assertThrows(ReglaDominioException.class, () -> servicio.crear(alojamiento, apartamento, repetido, AHORA));
    }

    @Test
    @DisplayName("Creación: RN-02 los ocupantes no pueden superar la capacidad (el titular cuenta)")
    void rechazaExcederLaCapacidad() {
        List<Ocupante> cuatroAcompanantes = List.of(adulto("1000000002"), adulto("1000000003"),
                adulto("1000000004"), adulto("1000000005"));
        SolicitudReserva cincoOcupantes = solicitud(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3), cuatroAcompanantes, false, null);

        assertThrows(ReglaDominioException.class, () -> servicio.crear(alojamiento, apartamento, cincoOcupantes, AHORA));
    }

    @Test
    @DisplayName("Creación: RN-Propia-03 la mascota ocupa un cupo: 4 ocupantes con mascota no caben en un apartamento de 4")
    void laMascotaOcupaUnCupo() {
        List<Ocupante> tresAcompanantes = List.of(adulto("1000000002"), adulto("1000000003"), adulto("1000000004"));
        SolicitudReserva cuatroConMascota = solicitud(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3), tresAcompanantes, true, null);
        SolicitudReserva cuatroSinMascota = solicitud(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3), tresAcompanantes, false, null);

        assertThrows(ReglaDominioException.class, () -> servicio.crear(alojamiento, apartamento, cuatroConMascota, AHORA));
        assertEquals(4, servicio.crear(alojamiento, apartamento, cuatroSinMascota, AHORA).getOcupantes().size());
    }

    @Test
    @DisplayName("Creación: RN-Propia-03 con mascota y un cupo libre la reserva se crea y queda marcada")
    void aceptaMascotaSiHayCupo() {
        List<Ocupante> dosAcompanantes = List.of(adulto("1000000002"), adulto("1000000003"));
        SolicitudReserva tresConMascota = solicitud(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3), dosAcompanantes, true, null);

        assertTrue(servicio.crear(alojamiento, apartamento, tresConMascota, AHORA).isConMascota());
    }

    @Test
    @DisplayName("Creación: RN-01 no se crea una reserva que se solapa con otra activa del mismo apartamento")
    void rechazaSolapamientoConReservaActiva() {
        reservaRepository.guardar(servicio.crear(alojamiento, apartamento, solicitudBasica(), AHORA));
        SolicitudReserva solapada = solicitud(LocalDate.of(2026, 12, 2), LocalDate.of(2026, 12, 5), List.of(), false, null);

        assertThrows(ReglaDominioException.class, () -> servicio.crear(alojamiento, apartamento, solapada, AHORA));
    }

    @Test
    @DisplayName("Creación: 3.1 una estancia que empieza el día de salida de otra no se solapa")
    void aceptaEstanciaQueEmpiezaElDiaDeSalida() {
        reservaRepository.guardar(servicio.crear(alojamiento, apartamento, solicitudBasica(), AHORA));
        SolicitudReserva contigua = solicitud(LocalDate.of(2026, 12, 3), LocalDate.of(2026, 12, 5), List.of(), false, null);

        assertEquals("RES-2026-00002", servicio.crear(alojamiento, apartamento, contigua, AHORA).getCodigo().valor());
    }

    @Test
    @DisplayName("Creación: RN-12 una reserva cancelada libera sus noches")
    void unaReservaCanceladaLiberaLasNoches() {
        Reserva existente = servicio.crear(alojamiento, apartamento, solicitudBasica(), AHORA);
        existente.cancelar(null);
        reservaRepository.guardar(existente);

        assertEquals(EstadoReserva.PENDIENTE, servicio.crear(alojamiento, apartamento, solicitudBasica(), AHORA).getEstado());
    }

    @Test
    @DisplayName("Creación: RN-07 no se crea una reserva sobre noches bloqueadas")
    void rechazaNochesBloqueadas() {
        apartamento.agregarBloqueo(Bloqueo.crear(new Estancia(LocalDate.of(2026, 12, 2), LocalDate.of(2026, 12, 4)), "Mantenimiento"));

        assertThrows(ReglaDominioException.class, () -> servicio.crear(alojamiento, apartamento, solicitudBasica(), AHORA));
    }

    @Test
    @DisplayName("Creación: RN-Propia-02 una llegada nocturna agrega el recargo de 30.000 al folio")
    void llegadaNocturnaAgregaRecargo() {
        SolicitudReserva nocturna = solicitud(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3),
                List.of(adulto("1000000002")), false, LocalTime.of(23, 30));

        Reserva reserva = servicio.crear(alojamiento, apartamento, nocturna, AHORA);

        assertEquals(LocalTime.of(23, 30), reserva.getHoraEstimadaLlegada());
        assertEquals(Dinero.de(350_000), reserva.getFolio().calcularTotalCargos());
        assertEquals(Dinero.de(320_000), reserva.getCotizacion().valorTotal()); // el recargo no es parte de la cotización
    }

    @Test
    @DisplayName("Creación: RN-Propia-02 una llegada en horario normal no genera recargo")
    void llegadaDiurnaNoGeneraRecargo() {
        SolicitudReserva diurna = solicitud(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3),
                List.of(adulto("1000000002")), false, LocalTime.of(15, 0));

        Reserva reserva = servicio.crear(alojamiento, apartamento, diurna, AHORA);

        assertEquals(Dinero.de(320_000), reserva.getFolio().calcularTotalCargos());
    }

    @Test
    @DisplayName("Creación: la hora estimada de llegada solo se registra una vez")
    void horaEstimadaSoloSeRegistraUnaVez() {
        Reserva reserva = servicio.crear(alojamiento, apartamento, solicitudBasica(), AHORA);
        RecargoLlegadaNocturna recargo = new RecargoLlegadaNocturna(Dinero.de(30_000), LocalTime.of(22, 0), LocalTime.of(5, 0));
        reserva.registrarHoraEstimadaLlegada(LocalTime.of(16, 0), recargo);

        assertThrows(ReglaDominioException.class, () -> reserva.registrarHoraEstimadaLlegada(LocalTime.of(23, 0), recargo));
    }
}

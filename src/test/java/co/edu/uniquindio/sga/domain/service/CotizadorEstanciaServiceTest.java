package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DescuentoEstanciaProlongada;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.valueobject.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaAnticipo;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CotizadorEstanciaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 1);
    private static final LocalDate DESDE_SIEMPRE = LocalDate.of(2026, 1, 1);

    private CotizadorEstanciaService cotizador;
    private Alojamiento alojamiento;
    private Apartamento apartamento;
    private Ocupante adulto1;
    private Ocupante adulto2;
    private Ocupante nino;

    @BeforeEach
    void preparar() {
        cotizador = new CotizadorEstanciaService(new DescuentoEstanciaProlongada(5, new BigDecimal("0.10")));

        ParametrosAlojamiento parametros = new ParametrosAlojamiento(new UmbralEdadFacturable(12),
                new HorarioAlojamiento(LocalTime.of(15, 0), LocalTime.of(11, 0)),
                new TiempoPreparacion(3), new PlazoConfirmacion(24),
                new HoraLimiteNoShow(LocalTime.of(21, 0)), new PoliticaAnticipo(true, new BigDecimal("0.5")));
        alojamiento = Alojamiento.crear("Búnker Orbital YoRHa", "El Espacio", parametros, Temporada.crearBase("Base"),
                List.of(new TramoAntelacion(168, BigDecimal.ZERO), new TramoAntelacion(0, BigDecimal.ONE)), HOY);
        Temporada alta = Temporada.crear("Alta", LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 16));
        alojamiento.agregarTemporada(alta);

        apartamento = Apartamento.crear("ATK-001", "Attack Suite", 4,
                List.of(new Dormitorio("Dormitorio 1", "Cama doble")), EstadoOperativo.PREPARADO);
        apartamento.definirTarifa(temporadaBase().id(), Dinero.de(80_000), DESDE_SIEMPRE);
        apartamento.definirTarifa(alta.id(), Dinero.de(125_000), DESDE_SIEMPRE);

        adulto1 = Ocupante.crear("1000000001", "Adulto Uno", LocalDate.of(1990, 5, 1));
        adulto2 = Ocupante.crear("1000000002", "Adulto Dos", LocalDate.of(1992, 8, 20));
        nino = Ocupante.crear("1000000003", "Niña", LocalDate.of(2019, 3, 10));
    }

    private Temporada temporadaBase() {
        return alojamiento.temporadas().stream().filter(Temporada::esBase).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Cotizador: RN-05 suma noche por noche la tarifa de la temporada de cada noche por los ocupantes facturables")
    void sumaNocheAPocheSegunLaTemporada() {
        // 14 dic: base (80.000); 15 y 16 dic: alta (125.000). Un adulto facturable.
        Cotizacion cotizacion = cotizador.cotizar(alojamiento, apartamento,
                new Estancia(LocalDate.of(2026, 12, 14), LocalDate.of(2026, 12, 17)), List.of(adulto1));

        assertEquals(3, cotizacion.totalNoches());
        assertEquals("Base", cotizacion.desglosePorNoche().get(0).nombreTemporada());
        assertEquals("Alta", cotizacion.desglosePorNoche().get(1).nombreTemporada());
        assertEquals(Dinero.de(330_000), cotizacion.valorTotal());
    }

    @Test
    @DisplayName("Cotizador: RN-06 el menor de 12 años ocupa cupo pero no genera cobro")
    void elMenorNoSeFactura() {
        Cotizacion cotizacion = cotizador.cotizar(alojamiento, apartamento,
                new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3)), List.of(adulto1, adulto2, nino));

        assertEquals(2, cotizacion.desglosePorNoche().get(0).ocupantesFacturables());
        assertEquals(Dinero.de(320_000), cotizacion.valorTotal()); // 2 noches x 80.000 x 2 adultos
    }

    @Test
    @DisplayName("Cotizador: RN-06 la edad se evalúa a la fecha de entrada: quien cumple 12 ese día ya es facturable")
    void cumplir12ElDiaDeEntradaYaFactura() {
        Ocupante cumple12 = Ocupante.crear("1000000004", "Cumple doce", LocalDate.of(2014, 11, 1));
        Ocupante cumple12Despues = Ocupante.crear("1000000005", "Cumple doce después", LocalDate.of(2014, 11, 2));

        Cotizacion cotizacion = cotizador.cotizar(alojamiento, apartamento,
                new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3)),
                List.of(adulto1, cumple12, cumple12Despues));

        assertEquals(2, cotizacion.desglosePorNoche().get(0).ocupantesFacturables());
    }

    @Test
    @DisplayName("Cotizador: RN-Propia-01 desde 5 noches se descuenta el 10% de la tarifa de cada noche")
    void aplicaDescuentoDesdeCincoNoches() {
        // 13 y 14 dic: base (80.000 -> 72.000); 15, 16 y 17 dic: alta (125.000 -> 112.500). Dos adultos.
        Cotizacion cotizacion = cotizador.cotizar(alojamiento, apartamento,
                new Estancia(LocalDate.of(2026, 12, 13), LocalDate.of(2026, 12, 18)), List.of(adulto1, adulto2));

        assertEquals(Dinero.de(72_000), cotizacion.desglosePorNoche().get(0).tarifaAplicada());
        assertEquals(Dinero.de(112_500), cotizacion.desglosePorNoche().get(2).tarifaAplicada());
        assertEquals(Dinero.de(963_000), cotizacion.valorTotal());
    }

    @Test
    @DisplayName("Cotizador: RN-Propia-01 con 4 noches no hay descuento")
    void conCuatroNochesNoHayDescuento() {
        Cotizacion cotizacion = cotizador.cotizar(alojamiento, apartamento,
                new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 5)), List.of(adulto1));

        assertEquals(Dinero.de(320_000), cotizacion.valorTotal());
    }

    @Test
    @DisplayName("Cotizador: una estancia solo con menores no se puede cotizar")
    void rechazaEstanciaSinOcupantesFacturables() {
        Estancia estancia = new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));

        assertThrows(ReglaDominioException.class, () -> cotizador.cotizar(alojamiento, apartamento, estancia, List.of(nino)));
    }

    @Test
    @DisplayName("Cotizador: 7.4 si el apartamento no tiene tarifa para la temporada de una noche, no se cotiza")
    void rechazaSiFaltaLaTarifaDeUnaTemporada() {
        Apartamento sinTarifas = Apartamento.crear("ATK-009", "Sin tarifas", 4,
                List.of(new Dormitorio("Dormitorio 1", "Cama doble")), EstadoOperativo.PREPARADO);
        Estancia estancia = new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));

        assertThrows(ReglaDominioException.class, () -> cotizador.cotizar(alojamiento, sinTarifas, estancia, List.of(adulto1)));
    }

    @Test
    @DisplayName("Cotizador: RN-Propia-01 los parámetros del descuento se validan")
    void validaLosParametrosDelDescuento() {
        assertThrows(ReglaDominioException.class, () -> new DescuentoEstanciaProlongada(0, new BigDecimal("0.10")));
        assertThrows(ReglaDominioException.class, () -> new DescuentoEstanciaProlongada(5, new BigDecimal("1")));
        assertThrows(ReglaDominioException.class, () -> new DescuentoEstanciaProlongada(5, new BigDecimal("-0.1")));
    }
}

package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PoliticaCancelacionTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 1);

    private static List<TramoAntelacion> tramosDeLaFicha() {
        return List.of(
                new TramoAntelacion(7 * 24, BigDecimal.ZERO),
                new TramoAntelacion(3 * 24, new BigDecimal("0.50")),
                new TramoAntelacion(0, BigDecimal.ONE));
    }

    @Test
    @DisplayName("Política: RN-13 la retención sale del tramo que corresponde a la antelación")
    void retencionSegunLaAntelacion() {
        PoliticaCancelacion politica = PoliticaCancelacion.crear(1, HOY, tramosDeLaFicha());

        assertEquals(0, new BigDecimal("0").compareTo(politica.retencionAplicable(10 * 24)));
        assertEquals(0, new BigDecimal("0.50").compareTo(politica.retencionAplicable(5 * 24)));
        assertEquals(0, BigDecimal.ONE.compareTo(politica.retencionAplicable(24)));
    }

    @Test
    @DisplayName("Política: 7.4 exige al menos dos tramos")
    void exigeAlMenosDosTramos() {
        List<TramoAntelacion> unTramo = List.of(new TramoAntelacion(0, BigDecimal.ONE));

        assertThrows(ReglaDominioException.class, () -> PoliticaCancelacion.crear(1, HOY, unTramo));
    }

    @Test
    @DisplayName("Política: debe haber un tramo de 0 horas para cubrir toda cancelación")
    void exigeTramoDeCeroHoras() {
        List<TramoAntelacion> sinCero = List.of(
                new TramoAntelacion(168, BigDecimal.ZERO),
                new TramoAntelacion(72, new BigDecimal("0.50")));

        assertThrows(ReglaDominioException.class, () -> PoliticaCancelacion.crear(1, HOY, sinCero));
    }

    @Test
    @DisplayName("Política: dos tramos no pueden tener la misma antelación mínima")
    void rechazaAntelacionesRepetidas() {
        List<TramoAntelacion> repetidos = List.of(
                new TramoAntelacion(72, BigDecimal.ZERO),
                new TramoAntelacion(72, new BigDecimal("0.50")),
                new TramoAntelacion(0, BigDecimal.ONE));

        assertThrows(ReglaDominioException.class, () -> PoliticaCancelacion.crear(1, HOY, repetidos));
    }
}

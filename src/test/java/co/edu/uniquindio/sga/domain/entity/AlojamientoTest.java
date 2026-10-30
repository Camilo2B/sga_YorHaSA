package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlojamientoTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 1);

    private Alojamiento alojamiento;
    private Temporada alta;

    private static List<TramoAntelacion> tramos(String retencionLejana, String retencionCercana) {
        return List.of(new TramoAntelacion(168, new BigDecimal(retencionLejana)),
                new TramoAntelacion(0, new BigDecimal(retencionCercana)));
    }

    private static ParametrosAlojamiento parametros() {
        return new ParametrosAlojamiento(new UmbralEdadFacturable(12),
                new HorarioAlojamiento(LocalTime.of(15, 0), LocalTime.of(11, 0)),
                new TiempoPreparacion(3), new PlazoConfirmacion(24),
                new HoraLimiteNoShow(LocalTime.of(21, 0)), new PoliticaAnticipo(true, new BigDecimal("0.5")));
    }

    @BeforeEach
    void preparar() {
        alojamiento = Alojamiento.crear("Búnker Orbital YoRHa", "El Espacio", parametros(),
                Temporada.crearBase("Base"), tramos("0", "1"), HOY);
        alta = Temporada.crear("Alta", LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 16));
    }

    @Test
    @DisplayName("Alojamiento: F-07 y 7.4 nace con temporada base y política versión 1 vigente")
    void naceConBaseYPoliticaInicial() {
        assertEquals(1, alojamiento.temporadas().size());
        assertTrue(alojamiento.temporadas().get(0).esBase());
        assertEquals(1, alojamiento.politicaVigente(HOY).numeroVersion().numero());
    }

    @Test
    @DisplayName("Alojamiento: 7.4 la temporada inicial debe ser la base")
    void laTemporadaInicialDebeSerLaBase() {
        assertThrows(ReglaDominioException.class, () -> Alojamiento.crear("YoRHa", "El Espacio", parametros(),
                alta, tramos("0", "1"), HOY));
    }

    @Test
    @DisplayName("Alojamiento: 7.4 rechaza una temporada que se solapa con otra")
    void rechazaTemporadaSolapada() {
        // Arrange
        alojamiento.agregarTemporada(alta);
        Temporada solapada = Temporada.crear("Otra", LocalDate.of(2027, 1, 10), LocalDate.of(2027, 1, 20));

        // Act + Assert
        assertThrows(ReglaDominioException.class, () -> alojamiento.agregarTemporada(solapada));
    }

    @Test
    @DisplayName("Alojamiento: 7.4 no puede haber una segunda temporada base")
    void rechazaSegundaBase() {
        assertThrows(ReglaDominioException.class, () -> alojamiento.agregarTemporada(Temporada.crearBase("Otra base")));
    }

    @Test
    @DisplayName("Alojamiento: 7.4 ninguna fecha queda sin temporada (usa la específica o, si no hay, la base)")
    void todaFechaTieneTemporada() {
        // Arrange
        alojamiento.agregarTemporada(alta);

        // Act + Assert
        assertEquals("Alta", alojamiento.temporadaVigente(LocalDate.of(2026, 12, 20)).nombre());
        assertEquals("Base", alojamiento.temporadaVigente(LocalDate.of(2026, 5, 20)).nombre());
    }

    @Test
    @DisplayName("Alojamiento: 7.4 actualizar la política conserva la versión anterior")
    void nuevaPoliticaConservaLaAnterior() {
        // Act
        alojamiento.definirNuevaPolitica(tramos("0.2", "1"), HOY.plusDays(30));

        // Assert
        assertEquals(2, alojamiento.historialPoliticas().size());
        assertEquals(1, alojamiento.historialPoliticas().get(0).numeroVersion().numero());
    }

    @Test
    @DisplayName("Alojamiento: una nueva política debe regir después de la más reciente")
    void nuevaPoliticaDebeSerPosterior() {
        assertThrows(ReglaDominioException.class, () -> alojamiento.definirNuevaPolitica(tramos("0.2", "1"), HOY));
    }

    @Test
    @DisplayName("Alojamiento: 3.5 la política vigente depende de la fecha")
    void politicaVigenteSegunLaFecha() {
        // Arrange
        alojamiento.definirNuevaPolitica(tramos("0.2", "1"), HOY.plusDays(30));

        // Act + Assert
        assertEquals(1, alojamiento.politicaVigente(HOY.plusDays(29)).numeroVersion().numero());
        assertEquals(2, alojamiento.politicaVigente(HOY.plusDays(30)).numeroVersion().numero());
    }

    @Test
    @DisplayName("Alojamiento: RN-13 la retención se calcula con la versión congelada, no con la vigente")
    void retencionUsaLaVersionCongelada() {
        // Arrange: la reserva congeló la versión 1 (retención 0% con antelación); luego rige la versión 2 (50%)
        var versionCongelada = alojamiento.politicaVigente(HOY).numeroVersion();
        alojamiento.definirNuevaPolitica(tramos("0.5", "1"), HOY.plusDays(30));

        // Act
        BigDecimal conLaCongelada = alojamiento.politicaDeVersion(versionCongelada).orElseThrow().retencionAplicable(240);
        BigDecimal conLaVigente = alojamiento.politicaVigente(HOY.plusDays(40)).retencionAplicable(240);

        // Assert
        assertEquals(0, conLaCongelada.compareTo(BigDecimal.ZERO));
        assertEquals(0, conLaVigente.compareTo(new BigDecimal("0.5")));
    }
}

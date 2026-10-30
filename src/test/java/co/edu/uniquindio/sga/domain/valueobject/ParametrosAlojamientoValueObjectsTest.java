package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParametrosAlojamientoValueObjectsTest {

    @Test
    @DisplayName("VO UmbralEdadFacturable: RN-06 la edad que alcanza o supera el umbral es facturable")
    void umbralDeEdadFacturable() {
        // Arrange
        UmbralEdadFacturable umbral = new UmbralEdadFacturable(12);

        // Act + Assert
        assertTrue(umbral.alcanza(12));
        assertTrue(umbral.alcanza(30));
        assertFalse(umbral.alcanza(11));
    }

    @Test
    @DisplayName("VO UmbralEdadFacturable: rechaza un umbral negativo")
    void umbralNegativoSeRechaza() {
        assertThrows(ReglaDominioException.class, () -> new UmbralEdadFacturable(-1));
    }

    @Test
    @DisplayName("VO HorarioAlojamiento: 3.3 con 4 h entre salida y entrada, 3 h de preparación alcanzan")
    void recambioMismoDiaCuandoLaVentanaAlcanza() {
        // Arrange
        HorarioAlojamiento horario = new HorarioAlojamiento(LocalTime.of(15, 0), LocalTime.of(11, 0));

        // Act + Assert
        assertEquals(4, horario.ventanaDeRecambio().toHours());
        assertTrue(horario.permiteRecambioMismoDia(new TiempoPreparacion(3)));
        assertTrue(horario.permiteRecambioMismoDia(new TiempoPreparacion(4)));
    }

    @Test
    @DisplayName("VO HorarioAlojamiento: 3.3 si la preparación excede la ventana no se puede recibir el mismo día")
    void recambioMismoDiaCuandoLaVentanaNoAlcanza() {
        // Arrange
        HorarioAlojamiento horario = new HorarioAlojamiento(LocalTime.of(15, 0), LocalTime.of(11, 0));

        // Act + Assert
        assertFalse(horario.permiteRecambioMismoDia(new TiempoPreparacion(5)));
    }

    @Test
    @DisplayName("VO HoraLimiteNoShow: 7.5 se puede declarar desde la hora límite del día de entrada, no antes")
    void noShowSoloDesdeLaHoraLimite() {
        // Arrange
        HoraLimiteNoShow limite = new HoraLimiteNoShow(LocalTime.of(21, 0));
        LocalDate entrada = LocalDate.of(2026, 11, 10);

        // Act + Assert
        assertFalse(limite.permiteDeclarar(entrada, LocalDateTime.of(2026, 11, 10, 20, 59)));
        assertTrue(limite.permiteDeclarar(entrada, LocalDateTime.of(2026, 11, 10, 21, 0)));
        assertTrue(limite.permiteDeclarar(entrada, LocalDateTime.of(2026, 11, 11, 8, 0)));
    }

    @Test
    @DisplayName("VO PlazoConfirmacion: RN-21 vence solo cuando SUPERA el plazo")
    void plazoDeConfirmacionVenceAlSuperarse() {
        // Arrange
        PlazoConfirmacion plazo = new PlazoConfirmacion(24);
        LocalDateTime creada = LocalDateTime.of(2026, 11, 1, 10, 0);

        // Act + Assert
        assertFalse(plazo.haVencido(creada, LocalDateTime.of(2026, 11, 2, 10, 0)));
        assertTrue(plazo.haVencido(creada, LocalDateTime.of(2026, 11, 2, 10, 1)));
    }

    @Test
    @DisplayName("VO PlazoConfirmacion: rechaza un plazo de cero horas")
    void plazoCeroSeRechaza() {
        assertThrows(ReglaDominioException.class, () -> new PlazoConfirmacion(0));
    }

    @Test
    @DisplayName("VO PoliticaAnticipo: el anticipo es un porcentaje del valor de cada estancia")
    void anticipoEsPorcentajeDelValor() {
        // Arrange
        PoliticaAnticipo politica = new PoliticaAnticipo(true, new BigDecimal("0.50"));

        // Act
        Dinero anticipo = politica.calcularAnticipo(Dinero.de(500001));

        // Assert (redondeo al peso más cercano, 3.4)
        assertEquals(Dinero.de(250001), anticipo);
    }

    @Test
    @DisplayName("VO PoliticaAnticipo: si no se exige anticipo, vale cero sin importar el porcentaje recibido")
    void sinAnticipoValeCero() {
        // Arrange
        PoliticaAnticipo politica = new PoliticaAnticipo(false, new BigDecimal("0.50"));

        // Act + Assert
        assertEquals(Dinero.cero(), politica.calcularAnticipo(Dinero.de(500000)));
    }

    @Test
    @DisplayName("VO PoliticaAnticipo: exigido, el porcentaje debe estar entre 0 (exclusivo) y 1")
    void porcentajeFueraDeRangoSeRechaza() {
        assertThrows(ReglaDominioException.class, () -> new PoliticaAnticipo(true, BigDecimal.ZERO));
        assertThrows(ReglaDominioException.class, () -> new PoliticaAnticipo(true, new BigDecimal("1.5")));
    }
}

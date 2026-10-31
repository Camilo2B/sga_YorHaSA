package co.edu.uniquindio.sga.domain.entity;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlojamientoPoliticaVersionesTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 1);

    private Alojamiento alojamiento;

    private static List<TramoAntelacion> tramos(String retencionLejana) {
        return List.of(new TramoAntelacion(168, new BigDecimal(retencionLejana)),
                new TramoAntelacion(0, BigDecimal.ONE));
    }

    @BeforeEach
    void preparar() {
        ParametrosAlojamiento parametros = new ParametrosAlojamiento(new UmbralEdadFacturable(12),
                new HorarioAlojamiento(LocalTime.of(15, 0), LocalTime.of(11, 0)),
                new TiempoPreparacion(3), new PlazoConfirmacion(24),
                new HoraLimiteNoShow(LocalTime.of(21, 0)), new PoliticaAnticipo(true, new BigDecimal("0.5")));
        alojamiento = Alojamiento.crear("Búnker Orbital YoRHa", "El Espacio", parametros,
                Temporada.crearBase("Base"), tramos("0"), HOY);
    }

    @Test
    @DisplayName("Alojamiento: 7.4 cada versión de la política se consulta por su número y las anteriores se conservan")
    void consultaCadaVersionPorNumero() {
        alojamiento.definirNuevaPolitica(tramos("0.25"), HOY.plusDays(30));

        assertEquals(2, alojamiento.historialPoliticas().size());
        assertEquals(0, BigDecimal.ZERO.compareTo(alojamiento.politicaPorNumero(1).orElseThrow().retencionAplicable(200)));
        assertEquals(0, new BigDecimal("0.25").compareTo(alojamiento.politicaPorNumero(2).orElseThrow().retencionAplicable(200)));
    }

    @Test
    @DisplayName("Alojamiento: una versión que no existe se devuelve vacía")
    void versionInexistenteVieneVacia() {
        assertTrue(alojamiento.politicaPorNumero(7).isEmpty());
    }
}

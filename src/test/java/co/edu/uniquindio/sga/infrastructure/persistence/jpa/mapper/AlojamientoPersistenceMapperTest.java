package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.valueobject.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.valueobject.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaAnticipo;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.AlojamientoEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlojamientoPersistenceMapperTest {

    private final AlojamientoPersistenceMapper mapper = new AlojamientoPersistenceMapper();

    private static ParametrosAlojamiento parametros() {
        return new ParametrosAlojamiento(new UmbralEdadFacturable(12),
                new HorarioAlojamiento(LocalTime.of(15, 0), LocalTime.of(11, 0)),
                new TiempoPreparacion(3), new PlazoConfirmacion(24),
                new HoraLimiteNoShow(LocalTime.of(21, 0)), new PoliticaAnticipo(true, new BigDecimal("0.50")));
    }

    @Test
    @DisplayName("Persistencia: ida y vuelta conserva el alojamiento, sus temporadas y todas las versiones de la política")
    void idaYVueltaConservaElAgregado() {
        // Arrange
        LocalDate hoy = LocalDate.of(2026, 10, 1);
        Alojamiento original = Alojamiento.crear("Búnker Orbital YoRHa", "El Espacio", parametros(),
                Temporada.crearBase("Base"),
                List.of(new TramoAntelacion(168, BigDecimal.ZERO), new TramoAntelacion(0, BigDecimal.ONE)), hoy);
        original.agregarTemporada(Temporada.crear("Alta", LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 16)));
        original.definirNuevaPolitica(
                List.of(new TramoAntelacion(168, new BigDecimal("0.25")), new TramoAntelacion(0, BigDecimal.ONE)),
                hoy.plusDays(30));

        // Act
        AlojamientoEntity entity = mapper.toEntity(original);
        Alojamiento restaurado = mapper.toDomain(entity, parametros());

        // Assert
        assertEquals(original.id(), restaurado.id());
        assertEquals(original.nombre(), restaurado.nombre());
        assertEquals(original.ciudad(), restaurado.ciudad());
        assertEquals(parametros(), restaurado.parametros());
        assertEquals(2, restaurado.temporadas().size());
        assertTrue(restaurado.temporadas().stream().anyMatch(Temporada::esBase));
        Temporada alta = restaurado.temporadaVigente(LocalDate.of(2026, 12, 20));
        assertEquals("Alta", alta.nombre());
        assertEquals(original.temporadaVigente(LocalDate.of(2026, 12, 20)).id(), alta.id());
        assertEquals(2, restaurado.historialPoliticas().size());
        PoliticaCancelacion v2 = restaurado.politicaVigente(hoy.plusDays(31));
        assertEquals(2, v2.numeroVersion().numero());
        assertEquals(0, v2.retencionAplicable(200).compareTo(new BigDecimal("0.25")));
        assertEquals(0, restaurado.politicaVigente(hoy).retencionAplicable(200).compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Persistencia: guardar dos veces el mismo alojamiento genera los mismos ids de política (no recrea filas)")
    void losIdsDePoliticaSonEstables() {
        // Arrange
        Alojamiento alojamiento = Alojamiento.crear("YoRHa", "El Espacio", parametros(), Temporada.crearBase("Base"),
                List.of(new TramoAntelacion(168, BigDecimal.ZERO), new TramoAntelacion(0, BigDecimal.ONE)),
                LocalDate.of(2026, 10, 1));

        // Act
        var primera = mapper.toEntity(alojamiento).getPoliticas().get(0).getId();
        var segunda = mapper.toEntity(alojamiento).getPoliticas().get(0).getId();

        // Assert
        assertEquals(primera, segunda);
    }
}

package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.request.RegistrarTemporadaRequest;
import co.edu.uniquindio.sga.application.dto.response.AlojamientoResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarAlojamientoUseCase;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.valueobject.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaAnticipo;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlojamientoMapperTest {

    private final AlojamientoMapper mapper = Mappers.getMapper(AlojamientoMapper.class);

    @Test
    @DisplayName("API: el response del alojamiento trae parámetros, temporadas y la política vigente con su devolución")
    void construyeLaRespuestaDelAlojamiento() {
        // Arrange
        LocalDate hoy = LocalDate.of(2026, 10, 1);
        Alojamiento alojamiento = Alojamiento.crear("Búnker Orbital YoRHa", "El Espacio",
                new ParametrosAlojamiento(new UmbralEdadFacturable(12),
                        new HorarioAlojamiento(LocalTime.of(15, 0), LocalTime.of(11, 0)),
                        new TiempoPreparacion(3), new PlazoConfirmacion(24),
                        new HoraLimiteNoShow(LocalTime.of(21, 0)), new PoliticaAnticipo(true, new BigDecimal("0.50"))),
                Temporada.crearBase("Base"),
                List.of(new TramoAntelacion(168, BigDecimal.ZERO), new TramoAntelacion(0, BigDecimal.ONE)), hoy);
        var resultado = new ConsultarAlojamientoUseCase.Resultado(alojamiento, alojamiento.politicaVigente(hoy));

        // Act
        AlojamientoResponse response = mapper.toResponse(resultado);

        // Assert
        assertEquals("Búnker Orbital YoRHa", response.nombre());
        assertEquals(12, response.parametros().umbralEdadFacturable());
        assertEquals(LocalTime.of(15, 0), response.parametros().horaEntrada());
        assertEquals(3, response.parametros().tiempoPreparacionHoras());
        assertTrue(response.parametros().exigeAnticipo());
        assertEquals(0, response.parametros().porcentajeAnticipo().compareTo(new BigDecimal("0.50")));
        assertEquals(1, response.temporadas().size());
        assertTrue(response.temporadas().get(0).esBase());
        assertEquals(1, response.politicaVigente().version());
        assertEquals(2, response.politicaVigente().tramos().size());
        assertEquals(0, response.politicaVigente().tramos().get(0).porcentajeDevolucion().compareTo(BigDecimal.ONE));
    }

    @Test
    @DisplayName("API: el request de temporada pasa por el dominio (fechas invertidas lanzan ReglaDominioException)")
    void laTemporadaNoSeSaltaLasValidaciones() {
        // Arrange
        var invertida = new RegistrarTemporadaRequest("Alta", LocalDate.of(2027, 1, 16), LocalDate.of(2026, 12, 15));

        // Act + Assert
        assertThrows(ReglaDominioException.class, () -> mapper.toDomain(invertida));
    }

    @Test
    @DisplayName("API: el mapper convierte un request válido en una Temporada de dominio y de vuelta en response")
    void convierteTemporada() {
        // Arrange
        var request = new RegistrarTemporadaRequest("Alta", LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 16));

        // Act
        Temporada temporada = mapper.toDomain(request);
        var response = mapper.toTemporadaResponse(temporada);

        // Assert
        assertEquals(temporada.id(), response.id());
        assertEquals("Alta", response.nombre());
        assertEquals(LocalDate.of(2026, 12, 15), response.fechaInicio());
        assertEquals(false, response.esBase());
    }
}

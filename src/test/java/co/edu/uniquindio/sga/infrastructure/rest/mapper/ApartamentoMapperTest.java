package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.request.CrearApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.request.DormitorioRequest;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoResponse;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApartamentoMapperTest {

    private final ApartamentoMapper mapper = Mappers.getMapper(ApartamentoMapper.class);

    @Test
    @DisplayName("API: el mapper convierte el request en un Apartamento de dominio y de vuelta en un response")
    void convierteRequestADominioYDominioAResponse() {
        // Arrange
        CrearApartamentoRequest request = new CrearApartamentoRequest("ATK-001", "Attack Suite", 4,
                List.of(new DormitorioRequest("Dormitorio 1", "Cama doble")), EstadoOperativo.PREPARADO, null);

        // Act
        Apartamento dominio = mapper.toDomain(request);
        ApartamentoResponse response = mapper.toResponse(dominio);

        // Assert
        assertEquals("ATK-001", dominio.identificacion());
        assertEquals("ATK-001", response.identificacion());
        assertEquals("Attack Suite", response.nombre());
        assertEquals(4, response.capacidad());
        assertEquals(1, response.dormitorios().size());
        assertEquals("Cama doble", response.dormitorios().get(0).tipoCama());
        assertEquals(EstadoOperativo.PREPARADO, response.estadoOperativo());
        assertTrue(response.activo());
        // Sin indicarlo, el apartamento no permite mascotas (RN-Propia-03)
        assertFalse(response.permiteMascotas());
    }

    @Test
    @DisplayName("API: el mapper conserva si el apartamento permite mascotas (RN-Propia-03)")
    void conservaSiPermiteMascotas() {
        CrearApartamentoRequest request = new CrearApartamentoRequest("ATK-003", "Con mascotas", 4,
                List.of(new DormitorioRequest("Dormitorio 1", "Cama doble")), EstadoOperativo.PREPARADO, true);

        assertTrue(mapper.toResponse(mapper.toDomain(request)).permiteMascotas());
    }

    @Test
    @DisplayName("API: una regla de dominio violada no se salta por el mapper (capacidad cero lanza ReglaDominioException)")
    void elMapperNoSaltaLasValidacionesDelDominio() {
        // Arrange
        CrearApartamentoRequest request = new CrearApartamentoRequest("ATK-002", "Sin capacidad", 0,
                List.of(new DormitorioRequest("Dormitorio 1", "Cama doble")), EstadoOperativo.PREPARADO, null);

        // Act + Assert
        assertThrows(ReglaDominioException.class, () -> mapper.toDomain(request));
    }
}

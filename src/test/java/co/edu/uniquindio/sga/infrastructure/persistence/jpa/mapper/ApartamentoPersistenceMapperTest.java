package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.Gravedad;
import co.edu.uniquindio.sga.domain.valueobject.TipoApartamento;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ApartamentoEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ApartamentoPersistenceMapperTest {

    private final ApartamentoPersistenceMapper mapper = new ApartamentoPersistenceMapper();

    @Test
    @DisplayName("Persistencia: ida y vuelta conserva todo el agregado Apartamento sin perder datos ni identidades")
    void idaYVueltaConservaElAgregado() {
        // Arrange
        UUID temporadaId = UUID.randomUUID();
        Apartamento original = Apartamento.crear("ATK-001", "Attack Suite", 4,
                List.of(new Dormitorio("Dormitorio 1", "Cama doble"), new Dormitorio("Dormitorio 2", "Cama doble")),
                new TipoApartamento("Attack", List.of("Zona de práctica", "Cocina minimalista")),
                EstadoOperativo.PREPARADO);
        original.agregarTarifa(Tarifa.crear(temporadaId, Dinero.de(80000), LocalDate.of(2026, 1, 1)));
        original.agregarBloqueo(Bloqueo.crear(new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 5)), "Mantenimiento"));
        original.registrarNovedad(Novedad.crear(LocalDate.of(2026, 10, 1), "Operario", "Lámpara dañada", Gravedad.BAJA));
        original.cambiarEstadoOperativo(EstadoOperativo.OCUPADO);

        // Act
        ApartamentoEntity entity = mapper.toEntity(original);
        Apartamento restaurado = mapper.toDomain(entity);

        // Assert
        assertEquals(original.identificacion(), restaurado.identificacion());
        assertEquals(original.nombre(), restaurado.nombre());
        assertEquals(original.capacidad(), restaurado.capacidad());
        assertEquals(original.dormitorios(), restaurado.dormitorios());
        assertEquals(original.tipo(), restaurado.tipo());
        assertEquals(EstadoOperativo.OCUPADO, restaurado.estadoOperativo());
        assertEquals(original.tarifas().get(0).id(), restaurado.tarifas().get(0).id());
        assertEquals(original.tarifas().get(0).valorPorNoche(), restaurado.tarifas().get(0).valorPorNoche());
        assertEquals(temporadaId, restaurado.tarifas().get(0).temporadaId());
        assertEquals(original.bloqueos().get(0).id(), restaurado.bloqueos().get(0).id());
        assertEquals(original.bloqueos().get(0).rango(), restaurado.bloqueos().get(0).rango());
        assertEquals(original.novedades().get(0).id(), restaurado.novedades().get(0).id());
        assertEquals(Gravedad.BAJA, restaurado.novedades().get(0).gravedad());
    }

    @Test
    @DisplayName("Persistencia: un apartamento desactivado (eliminación lógica) vuelve desactivado y sin tipo")
    void conservaEliminacionLogicaYTipoNulo() {
        // Arrange
        Apartamento original = Apartamento.crear("BTL-001", "Battle Suite", 2,
                List.of(new Dormitorio("Dormitorio 1", "Cama doble")), EstadoOperativo.PREPARADO);
        original.desactivar();

        // Act
        Apartamento restaurado = mapper.toDomain(mapper.toEntity(original));

        // Assert
        assertFalse(restaurado.activo());
        assertEquals(null, restaurado.tipo());
    }
}

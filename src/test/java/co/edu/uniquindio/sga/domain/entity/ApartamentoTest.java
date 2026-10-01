package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApartamentoTest {

    @Test
    @DisplayName("Prueba 5 (Entidad): No se puede crear un apartamento con capacidad menor o igual a cero")
    void lanzarExcepcionSiCapacidadEsInvalida() {
        // Arrange
        List<Dormitorio> dormitorios = List.of(new Dormitorio("Principal", "Doble"));

        // Act & Assert
        assertThrows(ReglaDominioException.class, () ->
                Apartamento.crear("APT-101", "Apt Deluxe", 0, dormitorios, EstadoOperativo.PREPARADO)
        );
    }

    @Test
    @DisplayName("Prueba 6 (Entidad): Transición de estado operativo inválida lanza excepción")
    void lanzarExcepcionEnTransicionEstadoOperativoInvalida() {
        // Arrange
        Apartamento apt = Apartamento.crear("APT-101", "Apt Deluxe", 4,
                List.of(new Dormitorio("Principal", "Doble")), EstadoOperativo.FUERA_DE_SERVICIO);

        // Act & Assert (FUERA_DE_SERVICIO no permite transición a OCUPADO)
        assertThrows(ReglaDominioException.class, () -> apt.cambiarEstadoOperativo(EstadoOperativo.OCUPADO));
    }
}
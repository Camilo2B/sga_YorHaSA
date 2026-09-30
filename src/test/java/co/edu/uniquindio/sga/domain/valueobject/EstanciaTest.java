package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EstanciaTest {

    @Test
    @DisplayName("Prueba 3 (VO): Verificación correcta de solapamiento entre dos estancias")
    void detectarSolapamientoEntreEstancias() {
        // Arrange
        Estancia estancia1 = new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10));
        Estancia estancia2 = new Estancia(LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 12));

        // Act
        boolean solapan = estancia1.seSolapaCon(estancia2);

        // Assert
        assertTrue(solapan);
    }

    @Test
    @DisplayName("Prueba 4 (VO): Crear estancia con fecha de salida anterior a entrada debe fallar")
    void lanzarExcepcionSiFechaSalidaEsAnteriorAEntrada() {
        // Arrange
        LocalDate entrada = LocalDate.of(2026, 11, 10);
        LocalDate salida = LocalDate.of(2026, 11, 5);

        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> new Estancia(entrada, salida));
    }
}
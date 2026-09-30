package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DineroTest {

    @Test
    @DisplayName("Prueba 1 (VO): Multiplicación correcta de monto por porcentaje")
    void multiplicarMontoPorPorcentajeCorrectamente() {
        // Arrange
        Dinero montoOriginal = Dinero.de(new BigDecimal("100000"));

        // Act
        Dinero resultado = montoOriginal.multiplicar(new BigDecimal("0.20"));

        // Assert
        assertEquals(Dinero.de(new BigDecimal("20000")), resultado);
    }

    @Test
    @DisplayName("Prueba 2 (VO): Creación de Dinero lanza excepción si el monto es negativo")
    void lanzarExcepcionSiMontoEsNegativo() {
        // Arrange & Act & Assert
        assertThrows(ReglaDominioException.class, () -> Dinero.de(new BigDecimal("-5000")));
    }
}
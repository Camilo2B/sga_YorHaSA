package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecargoLlegadaNocturnaTest {

    private final RecargoLlegadaNocturna recargo =
            new RecargoLlegadaNocturna(Dinero.de(30_000), LocalTime.of(22, 0), LocalTime.of(5, 0));

    @Test
    @DisplayName("Recargo nocturno: RN-Propia-02 la franja cruza la medianoche e incluye ambos extremos")
    void franjaQueCruzaLaMedianoche() {
        assertTrue(recargo.aplicaA(LocalTime.of(22, 0)));
        assertTrue(recargo.aplicaA(LocalTime.of(23, 59)));
        assertTrue(recargo.aplicaA(LocalTime.of(0, 0)));
        assertTrue(recargo.aplicaA(LocalTime.of(5, 0)));
        assertFalse(recargo.aplicaA(LocalTime.of(5, 1)));
        assertFalse(recargo.aplicaA(LocalTime.of(15, 0)));
        assertFalse(recargo.aplicaA(LocalTime.of(21, 59)));
    }

    @Test
    @DisplayName("Recargo nocturno: una franja dentro del mismo día también funciona")
    void franjaDentroDelMismoDia() {
        RecargoLlegadaNocturna mediodia = new RecargoLlegadaNocturna(Dinero.de(10_000), LocalTime.of(12, 0), LocalTime.of(14, 0));

        assertTrue(mediodia.aplicaA(LocalTime.of(13, 0)));
        assertFalse(mediodia.aplicaA(LocalTime.of(15, 0)));
    }

    @Test
    @DisplayName("Recargo nocturno: el valor debe ser mayor a cero y la franja no puede ser vacía")
    void validaSusParametros() {
        assertThrows(ReglaDominioException.class, () -> new RecargoLlegadaNocturna(Dinero.cero(), LocalTime.of(22, 0), LocalTime.of(5, 0)));
        assertThrows(ReglaDominioException.class, () -> new RecargoLlegadaNocturna(Dinero.de(30_000), LocalTime.of(22, 0), LocalTime.of(22, 0)));
    }
}

package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApartamentoTarifasTest {

    private static final LocalDate ENERO = LocalDate.of(2026, 1, 1);
    private static final LocalDate JUNIO = LocalDate.of(2026, 6, 1);

    private Apartamento apartamento;
    private UUID base;
    private UUID alta;

    @BeforeEach
    void preparar() {
        apartamento = Apartamento.crear("ATK-001", "Attack Suite", 4,
                List.of(new Dormitorio("Dormitorio 1", "Cama doble")), EstadoOperativo.PREPARADO);
        base = UUID.randomUUID();
        alta = UUID.randomUUID();
    }

    @Test
    @DisplayName("Apartamento: 7.4 una tarifa nueva conserva la anterior como histórico y pasa a ser la vigente")
    void tarifaNuevaConservaHistorico() {
        apartamento.definirTarifa(base, Dinero.de(80_000), ENERO);
        apartamento.definirTarifa(base, Dinero.de(90_000), JUNIO);

        assertEquals(2, apartamento.historialTarifas(base).size());
        assertEquals(Dinero.de(80_000), apartamento.tarifaVigente(base, LocalDate.of(2026, 3, 1)).valorPorNoche());
        assertEquals(Dinero.de(90_000), apartamento.tarifaVigente(base, LocalDate.of(2026, 7, 1)).valorPorNoche());
    }

    @Test
    @DisplayName("Apartamento: 7.4 el histórico se devuelve de la tarifa más reciente a la más antigua")
    void historialVieneDeLaMasRecienteALaMasAntigua() {
        apartamento.definirTarifa(base, Dinero.de(80_000), ENERO);
        apartamento.definirTarifa(base, Dinero.de(90_000), JUNIO);

        List<Tarifa> historial = apartamento.historialTarifas(base);

        assertEquals(JUNIO, historial.get(0).vigenteDesde());
        assertEquals(ENERO, historial.get(1).vigenteDesde());
    }

    @Test
    @DisplayName("Apartamento: 7.4 la tarifa nueva debe regir después de la más reciente de la misma temporada")
    void rechazaTarifaConVigenciaNoPosterior() {
        apartamento.definirTarifa(base, Dinero.de(80_000), JUNIO);

        assertThrows(ReglaDominioException.class, () -> apartamento.definirTarifa(base, Dinero.de(85_000), JUNIO));
        assertThrows(ReglaDominioException.class, () -> apartamento.definirTarifa(base, Dinero.de(85_000), ENERO));
        assertEquals(1, apartamento.historialTarifas(base).size());
    }

    @Test
    @DisplayName("Apartamento: 7.4 cada temporada lleva su propio histórico de tarifas")
    void otraTemporadaNoInterfiere() {
        apartamento.definirTarifa(base, Dinero.de(80_000), JUNIO);

        apartamento.definirTarifa(alta, Dinero.de(125_000), ENERO);

        assertEquals(1, apartamento.historialTarifas(base).size());
        assertEquals(1, apartamento.historialTarifas(alta).size());
        assertEquals(2, apartamento.historialTarifas().size());
    }

    @Test
    @DisplayName("Apartamento: 7.4 una tarifa en cero no es válida")
    void rechazaTarifaEnCero() {
        assertThrows(ReglaDominioException.class, () -> apartamento.definirTarifa(base, Dinero.cero(), ENERO));
        assertTrue(apartamento.historialTarifas().isEmpty());
    }

    @Test
    @DisplayName("Apartamento: 7.4 informa las temporadas que todavía no tienen tarifa")
    void informaTemporadasSinTarifa() {
        apartamento.definirTarifa(base, Dinero.de(80_000), ENERO);

        assertEquals(List.of(alta), apartamento.temporadasSinTarifa(List.of(base, alta)));
    }
}

package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApartamentoTest {

    @Test
    @DisplayName("No se puede crear un apartamento con capacidad menor o igual a cero")
    void capacidadInvalida() {
        assertThrows(ReglaDominioException.class, () ->
                Apartamento.crear("APT-1", "Prueba", 0, List.of(new Dormitorio("Dorm1", "Cama doble")), EstadoOperativo.PREPARADO));
    }

    @Test
    @DisplayName("No se puede agregar un bloqueo que se solape con otro existente")
    void bloqueoSolapado() {
        Apartamento apartamento = Apartamento.crear("APT-1", "Prueba", 2, List.of(new Dormitorio("Dorm1", "Cama doble")), EstadoOperativo.PREPARADO);
        apartamento.agregarBloqueo(Bloqueo.crear(new Estancia(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5)), "Mantenimiento"));
        assertThrows(ReglaDominioException.class, () ->
                apartamento.agregarBloqueo(Bloqueo.crear(new Estancia(LocalDate.of(2026, 12, 3), LocalDate.of(2026, 12, 7)), "Otro")));
    }

    private static Apartamento apartamento(int capacidad, boolean permiteMascotas) {
        return Apartamento.crear("APT-1", "Prueba", capacidad, List.of(new Dormitorio("Dorm1", "Cama doble")), null,
                permiteMascotas, EstadoOperativo.PREPARADO);
    }

    @Test
    @DisplayName("RN-Propia-03: la mascota ocupa un cupo y solo la recibe un apartamento que permite mascotas")
    void mascotaOcupaUnCupoYDebePermitirse() {
        Apartamento conMascotas = apartamento(4, true);
        Apartamento sinMascotas = apartamento(4, false);

        assertTrue(conMascotas.admiteGrupo(3, true));
        assertFalse(conMascotas.admiteGrupo(4, true));
        assertTrue(conMascotas.admiteGrupo(4, false));
        assertFalse(sinMascotas.admiteGrupo(1, true));
        assertThrows(ReglaDominioException.class, () -> sinMascotas.verificarAdmiteGrupo(1, true));
        assertThrows(ReglaDominioException.class, () -> conMascotas.verificarAdmiteGrupo(4, true));
    }

    @Test
    @DisplayName("RN-20: sin recambio el mismo día, una estancia contigua a una reserva activa no está disponible")
    void estanciaContiguaSegunRecambio() {
        Apartamento apartamento = apartamento(4, false);
        List<Estancia> reservadas = List.of(new Estancia(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5)));
        Estancia queEmpiezaElDiaDeSalida = new Estancia(LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 7));

        assertTrue(apartamento.estaDisponible(queEmpiezaElDiaDeSalida, 2, false, reservadas, true));
        assertFalse(apartamento.estaDisponible(queEmpiezaElDiaDeSalida, 2, false, reservadas, false));
    }

    @Test
    @DisplayName("7.3: la capacidad cambia con un método de negocio, sigue siendo positiva y no cambia si está retirado")
    void cambiarCapacidad() {
        Apartamento apartamento = apartamento(4, false);

        apartamento.cambiarCapacidad(6);
        assertEquals(6, apartamento.capacidad());
        assertThrows(ReglaDominioException.class, () -> apartamento.cambiarCapacidad(0));

        apartamento.desactivar();
        assertThrows(ReglaDominioException.class, () -> apartamento.cambiarCapacidad(2));
        assertThrows(ReglaDominioException.class, apartamento::desactivar);
    }
}

package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.ApartamentoRepositoryInMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroApartamentoServiceTest {

    private ApartamentoRepositoryInMemory apartamentoRepository;
    private RegistroApartamentoService servicio;

    @BeforeEach
    void preparar() {
        apartamentoRepository = new ApartamentoRepositoryInMemory();
        servicio = new RegistroApartamentoService(apartamentoRepository);
    }

    private Apartamento apartamento(String identificacion) {
        return Apartamento.crear(identificacion, "Attack Suite", 4,
                List.of(new Dormitorio("Dormitorio principal", "Cama doble")), EstadoOperativo.PREPARADO);
    }

    @Test
    @DisplayName("Prueba 13 (Servicio): registra un apartamento con identificación nueva")
    void registraApartamentoNuevo() {
        // Arrange
        Apartamento nuevo = apartamento("ATK-001");

        // Act
        servicio.registrar(nuevo);

        // Assert
        assertTrue(apartamentoRepository.buscarPorId("ATK-001").isPresent());
    }

    @Test
    @DisplayName("Prueba 14 (Servicio): L-04 rechaza una identificación repetida y no pisa el original")
    void rechazaIdentificacionRepetida() {
        // Arrange
        Apartamento original = apartamento("ATK-001");
        servicio.registrar(original);

        // Act + Assert
        assertThrows(ReglaDominioException.class, () -> servicio.registrar(apartamento("ATK-001")));
        assertEquals(1, apartamentoRepository.obtenerTodos().size());
    }
}

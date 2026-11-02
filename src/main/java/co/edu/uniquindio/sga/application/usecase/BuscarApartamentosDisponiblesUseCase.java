package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: buscar apartamentos disponibles por fechas y composición del grupo (7.5). Orquesta; las reglas
 * (RN-01, RN-02, RN-07, RN-20 y RN-Propia-03) viven en DisponibilidadApartamentoService y en Apartamento.
 */
@Service
public class BuscarApartamentosDisponiblesUseCase {

    private final AlojamientoRepository alojamientoRepository;
    private final DisponibilidadApartamentoService disponibilidadService;

    public BuscarApartamentosDisponiblesUseCase(AlojamientoRepository alojamientoRepository,
                                                DisponibilidadApartamentoService disponibilidadService) {
        this.alojamientoRepository = alojamientoRepository;
        this.disponibilidadService = disponibilidadService;
    }

    @Transactional(readOnly = true)
    public Page<Apartamento> ejecutar(Estancia estancia, int totalOcupantes, boolean conMascota, Pageable pageable) {
        Alojamiento alojamiento = alojamientoRepository.obtener().orElseThrow(AlojamientoNoEncontradoException::new);
        return Paginacion.paginar(
                disponibilidadService.buscarDisponibles(estancia, totalOcupantes, conMascota, alojamiento.parametros()),
                pageable);
    }
}

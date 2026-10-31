package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Caso de uso: cotizar una estancia (7.4). Solo consulta y orquesta; el cálculo vive en CotizadorEstanciaService. */
@Service
public class CotizarEstanciaUseCase {

    private final AlojamientoRepository alojamientoRepository;
    private final ApartamentoRepository apartamentoRepository;
    private final CotizadorEstanciaService cotizadorEstanciaService;

    public CotizarEstanciaUseCase(AlojamientoRepository alojamientoRepository,
                                  ApartamentoRepository apartamentoRepository,
                                  CotizadorEstanciaService cotizadorEstanciaService) {
        this.alojamientoRepository = alojamientoRepository;
        this.apartamentoRepository = apartamentoRepository;
        this.cotizadorEstanciaService = cotizadorEstanciaService;
    }

    @Transactional(readOnly = true)
    public Cotizacion ejecutar(String identificacionApartamento, Estancia estancia, List<Ocupante> ocupantes) {
        Alojamiento alojamiento = alojamientoRepository.obtener().orElseThrow(AlojamientoNoEncontradoException::new);
        Apartamento apartamento = apartamentoRepository.buscarPorId(identificacionApartamento)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacionApartamento));
        return cotizadorEstanciaService.cotizar(alojamiento, apartamento, estancia, ocupantes);
    }
}

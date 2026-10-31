package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.request.CotizarEstanciaRequest;
import co.edu.uniquindio.sga.application.dto.response.CotizacionResponse;
import co.edu.uniquindio.sga.application.usecase.CotizarEstanciaUseCase;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.CotizacionMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller delgado: valida el request, traduce con el mapper y delega. Cotizar no crea nada, por eso responde 200. */
@RestController
@RequestMapping("/api/cotizaciones")
public class CotizacionController {

    private final CotizarEstanciaUseCase cotizarEstanciaUseCase;
    private final CotizacionMapper mapper;

    public CotizacionController(CotizarEstanciaUseCase cotizarEstanciaUseCase, CotizacionMapper mapper) {
        this.cotizarEstanciaUseCase = cotizarEstanciaUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public CotizacionResponse cotizar(@Valid @RequestBody CotizarEstanciaRequest request) {
        Estancia estancia = mapper.toEstancia(request);
        Cotizacion cotizacion = cotizarEstanciaUseCase.ejecutar(
                request.identificacionApartamento(), estancia, mapper.toOcupantes(request.ocupantes()));
        return mapper.toResponse(request.identificacionApartamento(), estancia, cotizacion);
    }
}

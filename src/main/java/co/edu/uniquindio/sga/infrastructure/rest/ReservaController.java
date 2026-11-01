package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.request.CrearReservaRequest;
import co.edu.uniquindio.sga.application.dto.response.ReservaDetalleResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ReservaMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/** Controller delgado: valida el request, traduce con el mapper y delega en el caso de uso. */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final CrearReservaUseCase crearReservaUseCase;
    private final ConsultarReservaUseCase consultarReservaUseCase;
    private final ReservaMapper mapper;

    public ReservaController(CrearReservaUseCase crearReservaUseCase,
                             ConsultarReservaUseCase consultarReservaUseCase,
                             ReservaMapper mapper) {
        this.crearReservaUseCase = crearReservaUseCase;
        this.consultarReservaUseCase = consultarReservaUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ReservaDetalleResponse> crear(@Valid @RequestBody CrearReservaRequest request) {
        Reserva creada = crearReservaUseCase.ejecutar(request.identificacionApartamento(), mapper.toSolicitud(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(creada.getCodigo().valor())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toDetalleResponse(creada));
    }

    @GetMapping("/{codigo}")
    public ReservaDetalleResponse consultar(@PathVariable String codigo) {
        return mapper.toDetalleResponse(consultarReservaUseCase.ejecutar(mapper.toCodigo(codigo)));
    }
}

package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.request.RegistrarTemporadaRequest;
import co.edu.uniquindio.sga.application.dto.response.AlojamientoResponse;
import co.edu.uniquindio.sga.application.dto.response.TemporadaResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarAlojamientoUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarTemporadaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarTemporadaUseCase;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.AlojamientoMapper;
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
import java.util.UUID;

/** Controller delgado del recurso alojamiento: valida, traduce con el mapper y delega en el caso de uso. */
@RestController
@RequestMapping("/api/alojamiento")
public class AlojamientoController {

    private final ConsultarAlojamientoUseCase consultarAlojamientoUseCase;
    private final RegistrarTemporadaUseCase registrarTemporadaUseCase;
    private final ConsultarTemporadaUseCase consultarTemporadaUseCase;
    private final AlojamientoMapper mapper;

    public AlojamientoController(ConsultarAlojamientoUseCase consultarAlojamientoUseCase,
                                 RegistrarTemporadaUseCase registrarTemporadaUseCase,
                                 ConsultarTemporadaUseCase consultarTemporadaUseCase,
                                 AlojamientoMapper mapper) {
        this.consultarAlojamientoUseCase = consultarAlojamientoUseCase;
        this.registrarTemporadaUseCase = registrarTemporadaUseCase;
        this.consultarTemporadaUseCase = consultarTemporadaUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public AlojamientoResponse consultar() {
        return mapper.toResponse(consultarAlojamientoUseCase.ejecutar());
    }

    @PostMapping("/temporadas")
    public ResponseEntity<TemporadaResponse> registrarTemporada(@Valid @RequestBody RegistrarTemporadaRequest request) {
        Temporada creada = registrarTemporadaUseCase.ejecutar(mapper.toDomain(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toTemporadaResponse(creada));
    }

    @GetMapping("/temporadas/{id}")
    public TemporadaResponse consultarTemporada(@PathVariable UUID id) {
        return mapper.toTemporadaResponse(consultarTemporadaUseCase.ejecutar(id));
    }
}

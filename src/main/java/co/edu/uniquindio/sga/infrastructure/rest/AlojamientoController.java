package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.request.DefinirPoliticaCancelacionRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarTemporadaRequest;
import co.edu.uniquindio.sga.application.dto.response.AlojamientoResponse;
import co.edu.uniquindio.sga.application.dto.response.PoliticaCancelacionResponse;
import co.edu.uniquindio.sga.application.dto.response.TemporadaResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarAlojamientoUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarPoliticaCancelacionUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarTemporadaUseCase;
import co.edu.uniquindio.sga.application.usecase.DefinirPoliticaCancelacionUseCase;
import co.edu.uniquindio.sga.application.usecase.ListarPoliticasCancelacionUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarTemporadaUseCase;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
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
import java.util.List;
import java.util.UUID;

/** Controller delgado del recurso alojamiento: valida, traduce con el mapper y delega en el caso de uso. */
@RestController
@RequestMapping("/api/alojamiento")
public class AlojamientoController {

    private final ConsultarAlojamientoUseCase consultarAlojamientoUseCase;
    private final RegistrarTemporadaUseCase registrarTemporadaUseCase;
    private final ConsultarTemporadaUseCase consultarTemporadaUseCase;
    private final DefinirPoliticaCancelacionUseCase definirPoliticaCancelacionUseCase;
    private final ListarPoliticasCancelacionUseCase listarPoliticasCancelacionUseCase;
    private final ConsultarPoliticaCancelacionUseCase consultarPoliticaCancelacionUseCase;
    private final AlojamientoMapper mapper;

    public AlojamientoController(ConsultarAlojamientoUseCase consultarAlojamientoUseCase,
                                 RegistrarTemporadaUseCase registrarTemporadaUseCase,
                                 ConsultarTemporadaUseCase consultarTemporadaUseCase,
                                 DefinirPoliticaCancelacionUseCase definirPoliticaCancelacionUseCase,
                                 ListarPoliticasCancelacionUseCase listarPoliticasCancelacionUseCase,
                                 ConsultarPoliticaCancelacionUseCase consultarPoliticaCancelacionUseCase,
                                 AlojamientoMapper mapper) {
        this.consultarAlojamientoUseCase = consultarAlojamientoUseCase;
        this.registrarTemporadaUseCase = registrarTemporadaUseCase;
        this.consultarTemporadaUseCase = consultarTemporadaUseCase;
        this.definirPoliticaCancelacionUseCase = definirPoliticaCancelacionUseCase;
        this.listarPoliticasCancelacionUseCase = listarPoliticasCancelacionUseCase;
        this.consultarPoliticaCancelacionUseCase = consultarPoliticaCancelacionUseCase;
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

    /** F-07, 7.4: define una nueva versión de la política de cancelación; las anteriores se conservan. */
    @PostMapping("/politica-cancelacion")
    public ResponseEntity<PoliticaCancelacionResponse> definirPolitica(@Valid @RequestBody DefinirPoliticaCancelacionRequest request) {
        PoliticaCancelacion creada = definirPoliticaCancelacionUseCase.ejecutar(
                mapper.toTramos(request.tramos()), request.vigenteDesde());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{version}")
                .buildAndExpand(creada.numeroVersion().numero())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toPoliticaResponse(creada));
    }

    /** 7.4: todas las versiones de la política, de la primera a la más reciente. */
    @GetMapping("/politica-cancelacion")
    public List<PoliticaCancelacionResponse> listarPoliticas() {
        return mapper.toPoliticaResponses(listarPoliticasCancelacionUseCase.ejecutar());
    }

    @GetMapping("/politica-cancelacion/{version}")
    public PoliticaCancelacionResponse consultarPolitica(@PathVariable int version) {
        return mapper.toPoliticaResponse(consultarPoliticaCancelacionUseCase.ejecutar(version));
    }
}

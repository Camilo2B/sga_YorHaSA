package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.request.CrearApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearApartamentoUseCase;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ApartamentoMapper;
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

/** Controller delgado: valida el request (@Valid), traduce con el mapper y delega en el caso de uso. */
@RestController
@RequestMapping("/api/apartamentos")
public class ApartamentoController {

    private final CrearApartamentoUseCase crearApartamentoUseCase;
    private final ConsultarApartamentoUseCase consultarApartamentoUseCase;
    private final ApartamentoMapper mapper;

    public ApartamentoController(CrearApartamentoUseCase crearApartamentoUseCase,
                                 ConsultarApartamentoUseCase consultarApartamentoUseCase,
                                 ApartamentoMapper mapper) {
        this.crearApartamentoUseCase = crearApartamentoUseCase;
        this.consultarApartamentoUseCase = consultarApartamentoUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ApartamentoResponse> crear(@Valid @RequestBody CrearApartamentoRequest request) {
        Apartamento creado = crearApartamentoUseCase.ejecutar(mapper.toDomain(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{identificacion}")
                .buildAndExpand(creado.identificacion())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(creado));
    }

    @GetMapping("/{identificacion}")
    public ApartamentoResponse consultar(@PathVariable String identificacion) {
        return mapper.toResponse(consultarApartamentoUseCase.ejecutar(identificacion));
    }
}

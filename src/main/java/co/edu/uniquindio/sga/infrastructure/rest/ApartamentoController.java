package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.request.CrearApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.request.DefinirTarifaRequest;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoResponse;
import co.edu.uniquindio.sga.application.dto.response.TarifaResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.DefinirTarifaUseCase;
import co.edu.uniquindio.sga.application.usecase.ListarTarifasApartamentoUseCase;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ApartamentoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/** Controller delgado: valida el request (@Valid), traduce con el mapper y delega en el caso de uso. */
@RestController
@RequestMapping("/api/apartamentos")
public class ApartamentoController {

    private final CrearApartamentoUseCase crearApartamentoUseCase;
    private final ConsultarApartamentoUseCase consultarApartamentoUseCase;
    private final DefinirTarifaUseCase definirTarifaUseCase;
    private final ListarTarifasApartamentoUseCase listarTarifasApartamentoUseCase;
    private final ApartamentoMapper mapper;

    public ApartamentoController(CrearApartamentoUseCase crearApartamentoUseCase,
                                 ConsultarApartamentoUseCase consultarApartamentoUseCase,
                                 DefinirTarifaUseCase definirTarifaUseCase,
                                 ListarTarifasApartamentoUseCase listarTarifasApartamentoUseCase,
                                 ApartamentoMapper mapper) {
        this.crearApartamentoUseCase = crearApartamentoUseCase;
        this.consultarApartamentoUseCase = consultarApartamentoUseCase;
        this.definirTarifaUseCase = definirTarifaUseCase;
        this.listarTarifasApartamentoUseCase = listarTarifasApartamentoUseCase;
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

    /** 7.4: define una nueva tarifa (versión) del apartamento para una temporada. El histórico se conserva. */
    @PostMapping("/{identificacion}/tarifas")
    public ResponseEntity<TarifaResponse> definirTarifa(@PathVariable String identificacion,
                                                        @Valid @RequestBody DefinirTarifaRequest request) {
        Tarifa creada = definirTarifaUseCase.ejecutar(identificacion, request.temporadaId(),
                Dinero.de(request.valorPorNoche()), request.vigenteDesde());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(mapper.toTarifaResponse(creada));
    }

    /** 7.4: histórico de tarifas del apartamento (más recientes primero), con filtro opcional por temporada. */
    @GetMapping("/{identificacion}/tarifas")
    public List<TarifaResponse> listarTarifas(@PathVariable String identificacion,
                                              @RequestParam(required = false) UUID temporadaId) {
        return mapper.toTarifaResponses(listarTarifasApartamentoUseCase.ejecutar(identificacion, temporadaId));
    }
}

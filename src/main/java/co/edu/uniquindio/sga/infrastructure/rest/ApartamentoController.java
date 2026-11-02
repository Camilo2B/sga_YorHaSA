package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.request.BloquearApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.request.BuscarDisponibilidadRequest;
import co.edu.uniquindio.sga.application.dto.request.CambiarCapacidadRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.request.DefinirTarifaRequest;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoResponse;
import co.edu.uniquindio.sga.application.dto.response.BloqueoResponse;
import co.edu.uniquindio.sga.application.dto.response.TarifaResponse;
import co.edu.uniquindio.sga.application.usecase.BloquearApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.BuscarApartamentosDisponiblesUseCase;
import co.edu.uniquindio.sga.application.usecase.CambiarCapacidadApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarBloqueoUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarTarifaUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.DefinirTarifaUseCase;
import co.edu.uniquindio.sga.application.usecase.IniciarPreparacionApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.ListarApartamentosUseCase;
import co.edu.uniquindio.sga.application.usecase.ListarBloqueosApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.ListarTarifasApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.MarcarApartamentoPreparadoUseCase;
import co.edu.uniquindio.sga.application.usecase.RetirarApartamentoUseCase;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ApartamentoMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/** Controller delgado: valida el request (@Valid), traduce con el mapper y delega en el caso de uso. */
@RestController
@RequestMapping("/api/apartamentos")
public class ApartamentoController {

    private final CrearApartamentoUseCase crearApartamentoUseCase;
    private final ConsultarApartamentoUseCase consultarApartamentoUseCase;
    private final ListarApartamentosUseCase listarApartamentosUseCase;
    private final BuscarApartamentosDisponiblesUseCase buscarApartamentosDisponiblesUseCase;
    private final CambiarCapacidadApartamentoUseCase cambiarCapacidadApartamentoUseCase;
    private final RetirarApartamentoUseCase retirarApartamentoUseCase;
    private final BloquearApartamentoUseCase bloquearApartamentoUseCase;
    private final ListarBloqueosApartamentoUseCase listarBloqueosApartamentoUseCase;
    private final ConsultarBloqueoUseCase consultarBloqueoUseCase;
    private final DefinirTarifaUseCase definirTarifaUseCase;
    private final ListarTarifasApartamentoUseCase listarTarifasApartamentoUseCase;
    private final ConsultarTarifaUseCase consultarTarifaUseCase;
    private final IniciarPreparacionApartamentoUseCase iniciarPreparacionApartamentoUseCase;
    private final MarcarApartamentoPreparadoUseCase marcarApartamentoPreparadoUseCase;
    private final ApartamentoMapper mapper;

    public ApartamentoController(CrearApartamentoUseCase crearApartamentoUseCase,
                                 ConsultarApartamentoUseCase consultarApartamentoUseCase,
                                 ListarApartamentosUseCase listarApartamentosUseCase,
                                 BuscarApartamentosDisponiblesUseCase buscarApartamentosDisponiblesUseCase,
                                 CambiarCapacidadApartamentoUseCase cambiarCapacidadApartamentoUseCase,
                                 RetirarApartamentoUseCase retirarApartamentoUseCase,
                                 BloquearApartamentoUseCase bloquearApartamentoUseCase,
                                 ListarBloqueosApartamentoUseCase listarBloqueosApartamentoUseCase,
                                 ConsultarBloqueoUseCase consultarBloqueoUseCase,
                                 DefinirTarifaUseCase definirTarifaUseCase,
                                 ListarTarifasApartamentoUseCase listarTarifasApartamentoUseCase,
                                 ConsultarTarifaUseCase consultarTarifaUseCase,
                                 IniciarPreparacionApartamentoUseCase iniciarPreparacionApartamentoUseCase,
                                 MarcarApartamentoPreparadoUseCase marcarApartamentoPreparadoUseCase,
                                 ApartamentoMapper mapper) {
        this.crearApartamentoUseCase = crearApartamentoUseCase;
        this.consultarApartamentoUseCase = consultarApartamentoUseCase;
        this.listarApartamentosUseCase = listarApartamentosUseCase;
        this.buscarApartamentosDisponiblesUseCase = buscarApartamentosDisponiblesUseCase;
        this.cambiarCapacidadApartamentoUseCase = cambiarCapacidadApartamentoUseCase;
        this.retirarApartamentoUseCase = retirarApartamentoUseCase;
        this.bloquearApartamentoUseCase = bloquearApartamentoUseCase;
        this.listarBloqueosApartamentoUseCase = listarBloqueosApartamentoUseCase;
        this.consultarBloqueoUseCase = consultarBloqueoUseCase;
        this.definirTarifaUseCase = definirTarifaUseCase;
        this.listarTarifasApartamentoUseCase = listarTarifasApartamentoUseCase;
        this.consultarTarifaUseCase = consultarTarifaUseCase;
        this.iniciarPreparacionApartamentoUseCase = iniciarPreparacionApartamentoUseCase;
        this.marcarApartamentoPreparadoUseCase = marcarApartamentoPreparadoUseCase;
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

    /** 7.3: inventario completo (activos y retirados), 10 por página. */
    @GetMapping
    public Page<ApartamentoResponse> listar(@PageableDefault(size = 10) Pageable pageable) {
        return listarApartamentosUseCase.ejecutar(pageable).map(mapper::toResponse);
    }

    /** 7.5: apartamentos disponibles para unas fechas y la composición del grupo, 10 por página. */
    @GetMapping("/disponibles")
    public Page<ApartamentoResponse> buscarDisponibles(@Valid @ModelAttribute BuscarDisponibilidadRequest request,
                                                       @PageableDefault(size = 10) Pageable pageable) {
        return buscarApartamentosDisponiblesUseCase.ejecutar(mapper.toEstancia(request), mapper.toTotalOcupantes(request),
                mapper.toConMascota(request), pageable).map(mapper::toResponse);
    }

    @GetMapping("/{identificacion}")
    public ApartamentoResponse consultar(@PathVariable String identificacion) {
        return mapper.toResponse(consultarApartamentoUseCase.ejecutar(identificacion));
    }

    /** 7.3: cambia la capacidad; ninguna reserva activa puede quedar por encima de la nueva (RN-02). */
    @PutMapping("/{identificacion}/capacidad")
    public ApartamentoResponse cambiarCapacidad(@PathVariable String identificacion,
                                                @Valid @RequestBody CambiarCapacidadRequest request) {
        return mapper.toResponse(cambiarCapacidadApartamentoUseCase.ejecutar(identificacion, request.capacidad()));
    }

    /** 7.3 / 12.4: retira el apartamento de la venta (eliminación lógica), solo sin reservas activas ni futuras. */
    @PutMapping("/{identificacion}/retirar")
    public ApartamentoResponse retirar(@PathVariable String identificacion) {
        return mapper.toResponse(retirarApartamentoUseCase.ejecutar(identificacion));
    }

    /** RN-07: bloquea el apartamento en un rango de fechas sin reservas activas. */
    @PostMapping("/{id}/bloquear")
    public ResponseEntity<BloqueoResponse> bloquear(@PathVariable("id") String identificacion,
                                                    @Valid @RequestBody BloquearApartamentoRequest request) {
        Bloqueo creado = bloquearApartamentoUseCase.ejecutar(identificacion, mapper.toBloqueo(request));
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/apartamentos/{identificacion}/bloqueos/{id}")
                .buildAndExpand(identificacion, creado.id())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toBloqueoResponse(identificacion, creado));
    }

    @GetMapping("/{identificacion}/bloqueos")
    public Page<BloqueoResponse> listarBloqueos(@PathVariable String identificacion,
                                                @PageableDefault(size = 10) Pageable pageable) {
        return listarBloqueosApartamentoUseCase.ejecutar(identificacion, pageable)
                .map(bloqueo -> mapper.toBloqueoResponse(identificacion, bloqueo));
    }

    @GetMapping("/{identificacion}/bloqueos/{id}")
    public BloqueoResponse consultarBloqueo(@PathVariable String identificacion, @PathVariable UUID id) {
        return mapper.toBloqueoResponse(identificacion, consultarBloqueoUseCase.ejecutar(identificacion, id));
    }

    /** 7.4: define una nueva tarifa (versión) del apartamento para una temporada. El histórico se conserva. */
    @PostMapping("/{identificacion}/tarifas")
    public ResponseEntity<TarifaResponse> definirTarifa(@PathVariable String identificacion,
                                                        @Valid @RequestBody DefinirTarifaRequest request) {
        Tarifa creada = definirTarifaUseCase.ejecutar(identificacion, request.temporadaId(),
                mapper.toDinero(request.valorPorNoche()), request.vigenteDesde());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toTarifaResponse(creada));
    }

    /** 7.4: histórico de tarifas del apartamento (más recientes primero), con filtro opcional por temporada. */
    @GetMapping("/{identificacion}/tarifas")
    public Page<TarifaResponse> listarTarifas(@PathVariable String identificacion,
                                              @RequestParam(required = false) UUID temporadaId,
                                              @PageableDefault(size = 10) Pageable pageable) {
        return listarTarifasApartamentoUseCase.ejecutar(identificacion, temporadaId, pageable).map(mapper::toTarifaResponse);
    }

    @GetMapping("/{identificacion}/tarifas/{id}")
    public TarifaResponse consultarTarifa(@PathVariable String identificacion, @PathVariable UUID id) {
        return mapper.toTarifaResponse(consultarTarifaUseCase.ejecutar(identificacion, id));
    }

    /** 7.6: el personal de servicio empieza a preparar el apartamento (PENDIENTE_PREPARACION -> EN_PREPARACION). */
    @PutMapping("/{identificacion}/iniciar-preparacion")
    public ApartamentoResponse iniciarPreparacion(@PathVariable String identificacion) {
        return mapper.toResponse(iniciarPreparacionApartamentoUseCase.ejecutar(identificacion));
    }

    /** 7.6: el apartamento queda listo para el siguiente grupo (EN_PREPARACION -> PREPARADO). */
    @PutMapping("/{identificacion}/marcar-preparado")
    public ApartamentoResponse marcarPreparado(@PathVariable String identificacion) {
        return mapper.toResponse(marcarApartamentoPreparadoUseCase.ejecutar(identificacion));
    }
}

package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.request.CancelarReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.ConfirmarReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.ConsultarReservasRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearReservaRequest;
import co.edu.uniquindio.sga.application.dto.response.ReservaDetalleResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResumenResponse;
import co.edu.uniquindio.sga.application.usecase.CancelarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConfirmarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservasUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.DeclararNoShowUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarLlegadaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarSalidaUseCase;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ReservaMapper;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/** Controller delgado: valida el request, traduce con el mapper y delega en el caso de uso. */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final CrearReservaUseCase crearReservaUseCase;
    private final ConsultarReservaUseCase consultarReservaUseCase;
    private final ConsultarReservasUseCase consultarReservasUseCase;
    private final ConfirmarReservaUseCase confirmarReservaUseCase;
    private final CancelarReservaUseCase cancelarReservaUseCase;
    private final RegistrarLlegadaUseCase registrarLlegadaUseCase;
    private final RegistrarSalidaUseCase registrarSalidaUseCase;
    private final DeclararNoShowUseCase declararNoShowUseCase;
    private final ReservaMapper mapper;

    public ReservaController(CrearReservaUseCase crearReservaUseCase,
                             ConsultarReservaUseCase consultarReservaUseCase,
                             ConsultarReservasUseCase consultarReservasUseCase,
                             ConfirmarReservaUseCase confirmarReservaUseCase,
                             CancelarReservaUseCase cancelarReservaUseCase,
                             RegistrarLlegadaUseCase registrarLlegadaUseCase,
                             RegistrarSalidaUseCase registrarSalidaUseCase,
                             DeclararNoShowUseCase declararNoShowUseCase,
                             ReservaMapper mapper) {
        this.crearReservaUseCase = crearReservaUseCase;
        this.consultarReservaUseCase = consultarReservaUseCase;
        this.consultarReservasUseCase = consultarReservasUseCase;
        this.confirmarReservaUseCase = confirmarReservaUseCase;
        this.cancelarReservaUseCase = cancelarReservaUseCase;
        this.registrarLlegadaUseCase = registrarLlegadaUseCase;
        this.registrarSalidaUseCase = registrarSalidaUseCase;
        this.declararNoShowUseCase = declararNoShowUseCase;
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

    /** 7.5: listado con filtros opcionales, de la más reciente a la más antigua, 10 por página. */
    @GetMapping
    public Page<ReservaResumenResponse> consultar(@Valid @ModelAttribute ConsultarReservasRequest filtros,
                                                  @PageableDefault(size = 10) Pageable pageable) {
        return consultarReservasUseCase.ejecutar(filtros.estado(), filtros.identificacionApartamento(), filtros.desde(),
                filtros.hasta(), pageable).map(mapper::toResumenResponse);
    }

    @GetMapping("/{codigo}")
    public ReservaDetalleResponse consultarPorCodigo(@PathVariable String codigo) {
        return mapper.toDetalleResponse(consultarReservaUseCase.ejecutar(mapper.toCodigo(codigo)));
    }

    /** F-06: PENDIENTE -> CONFIRMADA cobrando el anticipo. El cuerpo es opcional. */
    @PutMapping("/{codigo}/confirmar")
    public ReservaDetalleResponse confirmar(@PathVariable String codigo,
                                            @Valid @RequestBody(required = false) ConfirmarReservaRequest request) {
        Reserva confirmada = confirmarReservaUseCase.ejecutar(mapper.toCodigo(codigo),
                mapper.toHoraEstimadaLlegada(request), mapper.toMedioPago(request), mapper.toReferencia(request));
        return mapper.toDetalleResponse(confirmada);
    }

    /** F-06: cancela con la política congelada en la reserva (RN-13) y libera sus noches (RN-12). */
    @PutMapping("/{codigo}/cancelar")
    public ReservaDetalleResponse cancelar(@PathVariable String codigo, @Valid @RequestBody CancelarReservaRequest request) {
        return mapper.toDetalleResponse(cancelarReservaUseCase.ejecutar(mapper.toCodigo(codigo), request.motivo()));
    }

    /** RN-10 y RN-11: check-in. La reserva pasa a EN_CURSO y el apartamento a OCUPADO. */
    @PutMapping("/{codigo}/registrar-llegada")
    public ReservaDetalleResponse registrarLlegada(@PathVariable String codigo) {
        return mapper.toDetalleResponse(registrarLlegadaUseCase.ejecutar(mapper.toCodigo(codigo)));
    }

    /** RN-17: check-out solo con el folio en paz y salvo. La reserva pasa a FINALIZADA y el apartamento a PENDIENTE_PREPARACION. */
    @PutMapping("/{codigo}/registrar-salida")
    public ReservaDetalleResponse registrarSalida(@PathVariable String codigo) {
        return mapper.toDetalleResponse(registrarSalidaUseCase.ejecutar(mapper.toCodigo(codigo)));
    }

    /** RN-12 y RN-13: no-show, solo pasada la hora límite del día de entrada. */
    @PutMapping("/{codigo}/no-show")
    public ReservaDetalleResponse declararNoShow(@PathVariable String codigo) {
        return mapper.toDetalleResponse(declararNoShowUseCase.ejecutar(mapper.toCodigo(codigo)));
    }
}

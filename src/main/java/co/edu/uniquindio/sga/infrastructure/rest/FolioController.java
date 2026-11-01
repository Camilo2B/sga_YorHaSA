package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.request.RegistrarPagoRequest;
import co.edu.uniquindio.sga.application.dto.response.FolioResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarFolioUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarPagoUseCase;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.FolioMapper;
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

/** Controller delgado del folio, sub-recurso de la reserva. No hay DELETE ni PUT: los movimientos no se modifican (RN-16). */
@RestController
@RequestMapping("/api/reservas/{codigo}/folio")
public class FolioController {

    private final ConsultarFolioUseCase consultarFolioUseCase;
    private final RegistrarPagoUseCase registrarPagoUseCase;
    private final FolioMapper mapper;

    public FolioController(ConsultarFolioUseCase consultarFolioUseCase, RegistrarPagoUseCase registrarPagoUseCase, FolioMapper mapper) {
        this.consultarFolioUseCase = consultarFolioUseCase;
        this.registrarPagoUseCase = registrarPagoUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public FolioResponse consultar(@PathVariable String codigo) {
        Folio folio = consultarFolioUseCase.ejecutar(mapper.toCodigo(codigo));
        return mapper.toResponse(codigo, folio);
    }

    /** RN-15: crea un movimiento en el folio. Responde con el folio actualizado y la URI del folio en Location. */
    @PostMapping("/pagos")
    public ResponseEntity<FolioResponse> registrarPago(@PathVariable String codigo, @Valid @RequestBody RegistrarPagoRequest request) {
        Folio folio = registrarPagoUseCase.ejecutar(mapper.toCodigo(codigo), mapper.toDinero(request.valor()),
                request.medioPago(), request.referencia());
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/reservas/{codigo}/folio")
                .buildAndExpand(codigo).toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(codigo, folio));
    }
}

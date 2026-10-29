package co.edu.uniquindio.sga.infrastructure.rest.exception;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.PoliticaCancelacionNoEncontradaException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradadException;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Traduce cada excepción a su código HTTP en un único formato (ErrorResponse).
 * 400 = forma del mensaje inválida; 404 = recurso inexistente (capa de aplicación);
 * 409 = regla de negocio violada (capa de dominio). 401 y 403 llegarán con la seguridad.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("VALIDACION", "La solicitud tiene campos inválidos", detalles));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarMensajeIlegible(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.de("SOLICITUD_MALFORMADA", "El cuerpo de la solicitud no es un JSON válido o tiene valores no permitidos"));
    }

    @ExceptionHandler({
            ApartamentoNoEncontradoException.class,
            ReservaNoEncontradadException.class,
            FolioNoEncontradoException.class,
            PoliticaCancelacionNoEncontradaException.class
    })
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.de("NO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(ReglaDominioException.class)
    public ResponseEntity<ErrorResponse> manejarReglaDominio(ReglaDominioException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.de("REGLA_DOMINIO", ex.getMessage()));
    }
}

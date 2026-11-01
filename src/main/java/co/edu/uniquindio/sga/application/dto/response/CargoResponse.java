package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;

import java.util.UUID;

/** Un cargo del folio, en pesos colombianos sin decimales. */
public record CargoResponse(UUID id, TipoCargo tipo, long valor) {
}

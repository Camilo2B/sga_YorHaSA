package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** El folio de una reserva. saldoPendiente = totalCargos - totalPagos (RN-15) y nunca es negativo. */
public record FolioResponse(
        String codigoReserva,
        List<CargoResponse> cargos,
        List<PagoResponse> pagos,
        long totalCargos,
        long totalPagos,
        long saldoPendiente,
        String moneda
) {
}

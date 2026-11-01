package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Detalle de una reserva. Los valores en dinero son pesos colombianos sin decimales (3.4). "ocupantes" incluye al
 * titular. valorTotal es el de la cotización congelada (hospedaje); totalCargosFolio suma además los cargos extra
 * (por ejemplo el recargo por llegada nocturna).
 */
public record ReservaDetalleResponse(
        String codigo,                      // "RES-2026-00001", no el objeto CodigoReserva
        EstadoReserva estado,
        CanalOrigen canalOrigen,
        String identificacionApartamento,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        int noches,
        LocalTime horaEstimadaLlegada,      // null hasta que se registre (RN-09)
        boolean conMascota,
        OcupanteResponse titular,
        List<OcupanteResponse> ocupantes,
        int totalOcupantes,
        long valorTotal,
        long totalCargosFolio,
        String moneda,                      // "COP"
        String versionPoliticaCancelacion,  // la congelada en la reserva (RN-22)
        LocalDate fechaCreacion,
        String motivoCancelacion,           // solo si la reserva fue cancelada
        Long retenidoPorCancelacion,        // del anticipo pagado, lo que se retiene (RN-13); null si no está cancelada
        Long devueltoPorCancelacion         // del anticipo pagado, lo que se devuelve; null si no está cancelada
) {
}

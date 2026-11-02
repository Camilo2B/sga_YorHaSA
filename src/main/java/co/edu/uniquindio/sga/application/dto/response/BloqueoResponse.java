package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.UUID;

/** Un bloqueo administrativo del apartamento: las noches [fechaInicio, fechaFin) no se venden (RN-07). */
public record BloqueoResponse(UUID id, String identificacionApartamento, LocalDate fechaInicio, LocalDate fechaFin,
                              String motivo) {
}

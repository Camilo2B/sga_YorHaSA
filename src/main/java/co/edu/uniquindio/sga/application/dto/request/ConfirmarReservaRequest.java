package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/**
 * PUT /api/reservas/{codigo}/confirmar. Todo el cuerpo es opcional:
 * <ul>
 *   <li>horaEstimadaLlegada: solo hace falta si la reserva se creó sin ella (RN-09 la exige para confirmar). Si ya
 *       la tenía, enviarla de nuevo da 409. Entre las 22:00 y las 05:00 agrega el recargo de RN-Propia-02.</li>
 *   <li>medioPago: con el que se paga el anticipo; solo se necesita si el alojamiento lo exige (50% por la Ficha).</li>
 * </ul>
 * Si falta algo que el dominio necesita, responde 409.
 */
public record ConfirmarReservaRequest(
        LocalTime horaEstimadaLlegada,

        MedioPago medioPago,

        @Size(max = 60, message = "La referencia del pago no puede superar 60 caracteres")
        String referencia
) {
}

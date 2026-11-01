package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Titular;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * Lo que se pide al crear una reserva, ya traducido a tipos del dominio. Los acompañantes no incluyen al titular
 * (3.2: el titular siempre es un ocupante más). La hora estimada de llegada es opcional al crear (RN-09 la exige
 * antes de confirmar).
 */
public record SolicitudReserva(Titular titular, List<Ocupante> acompanantes, Estancia estancia, CanalOrigen canalOrigen,
                               boolean conMascota, LocalTime horaEstimadaLlegada) {

    public SolicitudReserva {
        Objects.requireNonNull(titular, "El titular no puede ser nulo");
        Objects.requireNonNull(acompanantes, "Los acompañantes no pueden ser nulos");
        Objects.requireNonNull(estancia, "La estancia no puede ser nula");
        Objects.requireNonNull(canalOrigen, "El canal de origen no puede ser nulo");
        acompanantes = List.copyOf(acompanantes);
    }
}

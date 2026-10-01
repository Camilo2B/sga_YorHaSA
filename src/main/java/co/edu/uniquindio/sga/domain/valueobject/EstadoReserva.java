package co.edu.uniquindio.sga.domain.valueobject;

import java.util.Map;
import java.util.Set;

/**
 * Ciclo de vida de la reserva (sección 8). Las transiciones válidas viven
 * aquí mismo, no en Reserva ni en un servicio, para que RN-08 ("la reserva
 * solo transita entre los estados permitidos; toda transición inválida se
 * rechaza") sea imposible de romper por accidente.
 */
public enum EstadoReserva {
    PENDIENTE,
    CONFIRMADA,
    EN_CURSO,
    FINALIZADA,
    CANCELADA,
    NO_SHOW;

    private static final Map<EstadoReserva, Set<EstadoReserva>> TRANSICIONES_VALIDAS = Map.of(
            PENDIENTE, Set.of(CONFIRMADA, CANCELADA),
            CONFIRMADA, Set.of(EN_CURSO, CANCELADA, NO_SHOW),
            EN_CURSO, Set.of(FINALIZADA)
            // FINALIZADA, CANCELADA y NO_SHOW son estados terminales: sin salidas.
    );

    public boolean puedeTransitarA(EstadoReserva destino) {
        return TRANSICIONES_VALIDAS.getOrDefault(this, Set.of()).contains(destino);
    }

    /**
     * 8 - Se consideran reservas activas las que están en PENDIENTE,
     * CONFIRMADA o EN_CURSO. RN-12 depende de esta definición para saber
     * cuándo liberar las noches.
     */
    public boolean esActiva() {
        return this == PENDIENTE || this == CONFIRMADA || this == EN_CURSO;
    }

    public boolean esTerminal() {
        return this == FINALIZADA || this == CANCELADA || this == NO_SHOW;
    }
}
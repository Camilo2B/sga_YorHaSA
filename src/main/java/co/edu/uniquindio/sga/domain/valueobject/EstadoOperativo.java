package co.edu.uniquindio.sga.domain.valueobject;

import java.util.Map;
import java.util.Set;

/**
 * Condición física presente del apartamento (3.3).
 * <p>
 * Transiciones permitidas (7.6): PREPARADO→OCUPADO (registro),
 * OCUPADO→PENDIENTE_PREPARACION (salida),
 * PENDIENTE_PREPARACION→EN_PREPARACION→PREPARADO (servicio), y
 * FUERA_DE_SERVICIO desde cualquier estado NO ocupado, solo por administrador.
 * <p>
 * Nota de diseño: el enunciado no especifica una transición de salida desde
 * FUERA_DE_SERVICIO; se asume que solo el administrador la reactiva
 * manualmente (fuera de este enum), por eso no aparece como transición del
 * dominio.
 */
public enum EstadoOperativo {
    PREPARADO,
    OCUPADO,
    PENDIENTE_PREPARACION,
    EN_PREPARACION,
    FUERA_DE_SERVICIO;

    private static final Map<EstadoOperativo, Set<EstadoOperativo>> TRANSICIONES_VALIDAS = Map.of(
            PREPARADO, Set.of(OCUPADO, FUERA_DE_SERVICIO),
            OCUPADO, Set.of(PENDIENTE_PREPARACION), // OCUPADO es el único estado "ocupado": no admite FUERA_DE_SERVICIO directo
            PENDIENTE_PREPARACION, Set.of(EN_PREPARACION, FUERA_DE_SERVICIO),
            EN_PREPARACION, Set.of(PREPARADO, FUERA_DE_SERVICIO),
            FUERA_DE_SERVICIO, Set.of()
    );

    public boolean puedeTransitarA(EstadoOperativo destino) {
        return TRANSICIONES_VALIDAS.getOrDefault(this, Set.of()).contains(destino);
    }

    /**
     * RN-11: un apartamento solo puede recibir un grupo si su estado
     * operativo es PREPARADO.
     */
    public boolean permiteRegistro() {
        return this == PREPARADO;
    }
}
package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * L-09 / RN-06: edad a partir de la cual un ocupante genera cargo. El valor lo define cada alojamiento
 * (configuración), nunca el código.
 */
public record UmbralEdadFacturable(int anios) {

    public UmbralEdadFacturable {
        if (anios < 0) {
            throw new ReglaDominioException("El umbral de edad facturable no puede ser negativo");
        }
    }

    /** RN-06: facturable si la edad alcanza o supera el umbral. */
    public boolean alcanza(int edad) {
        return edad >= anios;
    }
}

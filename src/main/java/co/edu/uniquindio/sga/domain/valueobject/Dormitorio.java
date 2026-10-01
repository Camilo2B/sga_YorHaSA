package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * "Espacio para dormir dentro de un apartamento. Atributo descriptivo,
 * nunca reservable por separado." (sección 4).
 * <p>
 * Es Value Object -y no Entidad- porque no necesita identidad propia: se
 * define completamente por su descripción y tipo de cama, y se reemplaza
 * como conjunto si cambia la composición del apartamento.
 */
public record Dormitorio(String descripcion, String tipoCama) {

    public Dormitorio {
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaDominioException("La descripción del dormitorio no puede estar vacía");
        }
        if (tipoCama == null || tipoCama.isBlank()) {
            throw new ReglaDominioException("El tipo de cama del dormitorio no puede estar vacío");
        }
    }
}
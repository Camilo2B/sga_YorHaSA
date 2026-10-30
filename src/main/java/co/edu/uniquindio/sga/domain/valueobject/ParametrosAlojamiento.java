package co.edu.uniquindio.sga.domain.valueobject;

import java.util.Objects;

/**
 * Parámetros de la Ficha (sección 6 / A.4) agrupados en un solo objeto inmutable. El dominio no sabe de dónde
 * vienen (hoy, application.properties): quien lo construye es la capa de infraestructura.
 */
public record ParametrosAlojamiento(
        UmbralEdadFacturable umbralEdadFacturable,
        HorarioAlojamiento horario,
        TiempoPreparacion tiempoPreparacion,
        PlazoConfirmacion plazoConfirmacion,
        HoraLimiteNoShow horaLimiteNoShow,
        PoliticaAnticipo politicaAnticipo) {

    public ParametrosAlojamiento {
        Objects.requireNonNull(umbralEdadFacturable, "El umbral de edad facturable no puede ser nulo");
        Objects.requireNonNull(horario, "El horario no puede ser nulo");
        Objects.requireNonNull(tiempoPreparacion, "El tiempo de preparación no puede ser nulo");
        Objects.requireNonNull(plazoConfirmacion, "El plazo de confirmación no puede ser nulo");
        Objects.requireNonNull(horaLimiteNoShow, "La hora límite de no-show no puede ser nula");
        Objects.requireNonNull(politicaAnticipo, "La política de anticipo no puede ser nula");
    }
}

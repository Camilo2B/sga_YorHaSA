package co.edu.uniquindio.sga.domain.valueobject;

/**
 * Nivel de severidad de una Novedad. No estaba en el diagrama original como
 * tipo propio (la novedad solo tenía "+gravedad" sin tipo definido); se
 * introduce aquí como enum en vez de un String suelto para que no puedan
 * registrarse valores inventados fuera de este catálogo.
 * <p>
 * En el Corte 4, el modelo de IA (A.6 de la Ficha) sugiere este valor
 * automáticamente; con la IA deshabilitada, el operario lo selecciona
 * manualmente de este mismo catálogo — por eso el enum no cambia entre
 * ambos escenarios, solo cambia quién lo decide.
 */
public enum Gravedad {
    BAJA,
    MEDIA,
    ALTA
}
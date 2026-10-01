package co.edu.uniquindio.sga.domain.valueobject;

/**
 * L-18 - Medios de pago aceptados por el alojamiento. No estaba en el
 * diagrama original (Pago solo tenía "+medio" como String); se convierte en
 * enum por el mismo motivo que TipoCargo: un catálogo cerrado no debería
 * aceptar valores libres de texto.
 * <p>
 * Según la Ficha (A.5): transferencia bancaria (PSE/Nequi), tarjeta de
 * crédito/débito, efectivo en recepción.
 */
public enum MedioPago {
    TRANSFERENCIA,
    TARJETA,
    EFECTIVO
}
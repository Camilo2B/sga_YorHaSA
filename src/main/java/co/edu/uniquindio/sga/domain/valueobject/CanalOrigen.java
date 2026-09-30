package co.edu.uniquindio.sga.domain.valueobject;

/**
 * 2.4 - Canal de origen de una reserva: el alojamiento vende por tres
 * canales sobre el mismo inventario físico.
 * <p>
 * No confundir con CanalVenta (Corte 4): este enum solo etiqueta una
 * reserva puntual; CanalVenta es la configuración administrable del canal
 * como recurso del negocio (credencial, activo/inactivo).
 */
public enum CanalOrigen {
    PORTAL,
    DIRECTO,
    EXTERNO
}
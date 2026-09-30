package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 7.4 - Renglón del desglose noche por noche de una Cotización: fecha,
 * temporada, tarifa aplicada, ocupantes facturables y subtotal.
 * <p>
 * Guarda el NOMBRE de la temporada como copia congelada (snapshot), no una
 * referencia viva a la entidad Temporada: Temporada vive en el agregado
 * Alojamiento, y una Reserva nunca debe seguir apuntando a un objeto vivo de
 * otro agregado (3.5 - un cambio posterior de tarifas o temporadas no puede
 * alterar reservas ya creadas). Lo mismo aplica a tarifaAplicada: es el
 * valor ya congelado en el momento del cálculo, no un puntero a Tarifa.
 * <p>
 * El subtotal se calcula, nunca se guarda aparte, por la misma razón que
 * Reserva no guarda valorCongelado por separado de su Cotización.
 */
public record DetalleNoche(LocalDate fecha, String nombreTemporada, Dinero tarifaAplicada, int ocupantesFacturables) {

    public DetalleNoche {
        Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        if (nombreTemporada == null || nombreTemporada.isBlank()) {
            throw new ReglaDominioException("El nombre de la temporada no puede estar vacío");
        }
        Objects.requireNonNull(tarifaAplicada, "La tarifa aplicada no puede ser nula");
        if (tarifaAplicada.esNegativo()) {
            throw new ReglaDominioException("La tarifa aplicada no puede ser negativa");
        }
        if (ocupantesFacturables < 0) {
            throw new ReglaDominioException("El número de ocupantes facturables no puede ser negativo");
        }
    }

    /**
     * RN-05: la tarifa vigente multiplicada por el número de ocupantes
     * facturables, para esta noche específica.
     */
    public Dinero subtotal() {
        return tarifaAplicada.multiplicar(ocupantesFacturables);
    }
}
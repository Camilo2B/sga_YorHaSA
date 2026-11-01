package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Embeddable;

import java.time.LocalDate;

/** Persistencia de un Ocupante (o del Titular) dentro de la reserva: sin identidad propia, se identifica por documento. */
@Embeddable
public record OcupanteEmbeddable(String documento, String nombre, LocalDate fechaNacimiento) {
}

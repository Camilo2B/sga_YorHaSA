package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Embeddable;

import java.time.LocalDate;

/** Persistencia del Value Object Estancia: dos fechas, intervalo semiabierto [entrada, salida). */
@Embeddable
public record EstanciaEmbeddable(LocalDate entrada, LocalDate salida) {
}

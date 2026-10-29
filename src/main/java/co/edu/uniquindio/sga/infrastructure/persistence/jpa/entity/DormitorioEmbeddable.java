package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Embeddable;

/** Persistencia del Value Object Dormitorio (sin identidad propia: vive como colección del apartamento). */
@Embeddable
public record DormitorioEmbeddable(String descripcion, String tipoCama) {
}

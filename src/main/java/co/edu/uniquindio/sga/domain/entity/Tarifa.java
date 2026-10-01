package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * 4 / 7.4 - Valor por ocupante facturable, por noche, para un apartamento
 * en una temporada.
 * <p>
 * Entidad -y no Value Object- por el mismo argumento que PoliticaCancelacion:
 * 7.4 exige poder "consultar el histórico de tarifas", es decir, distinguir
 * qué tarifa regía en un momento pasado, no solo la vigente hoy. Eso
 * requiere identidad por versión.
 * <p>
 * Vive DENTRO del agregado Apartamento, pero
 * Temporada vive en el agregado Alojamiento. Por eso
 * aquí solo se guarda temporadaId (UUID), nunca un objeto Temporada: "un
 * agregado referencia a otro solo por su identificador, nunca por objeto".
 */
public final class Tarifa {

    private final UUID id;
    private final UUID temporadaId;
    private final Dinero valorPorNoche;
    private final LocalDate vigenteDesde;

    private Tarifa(UUID id, UUID temporadaId, Dinero valorPorNoche, LocalDate vigenteDesde) {
        this.id = id;
        this.temporadaId = temporadaId;
        this.valorPorNoche = valorPorNoche;
        this.vigenteDesde = vigenteDesde;
    }

    public static Tarifa crear(UUID temporadaId, Dinero valorPorNoche, LocalDate vigenteDesde) {
        Objects.requireNonNull(temporadaId, "El identificador de la temporada no puede ser nulo");
        Objects.requireNonNull(valorPorNoche, "El valor por noche no puede ser nulo");
        Objects.requireNonNull(vigenteDesde, "La fecha de vigencia no puede ser nula");
        if (valorPorNoche.esNegativo() || valorPorNoche.esCero()) {
            throw new ReglaDominioException("El valor de una tarifa debe ser positivo");
        }
        return new Tarifa(UUID.randomUUID(), temporadaId, valorPorNoche, vigenteDesde);
    }

    public UUID id() {
        return id;
    }

    public UUID temporadaId() {
        return temporadaId;
    }

    public Dinero valorPorNoche() {
        return valorPorNoche;
    }

    public LocalDate vigenteDesde() {
        return vigenteDesde;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tarifa otra)) return false;
        return this.id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
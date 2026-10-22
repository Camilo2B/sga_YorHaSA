package co.edu.uniquindio.sga.domain.valueobject;

import java.util.List;
import java.util.Objects;

public record TipoApartamento(String nombreTipo,List<String> caracteristicas) {

    public TipoApartamento{
        Objects.requireNonNull(nombreTipo, "El nombre del tipo no puede ser nulo");
        Objects.requireNonNull(caracteristicas, "Las caracteristicas no pueden ser nulas");
        caracteristicas = List.copyOf(caracteristicas);
    }
}

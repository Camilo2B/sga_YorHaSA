package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;

import java.util.List;

/** Un apartamento del inventario: respuesta de su creación (201), consulta, listados y operaciones (200). */
public record ApartamentoResponse(
        String identificacion,
        String nombre,
        int capacidad,
        List<DormitorioResponse> dormitorios,
        boolean permiteMascotas,
        EstadoOperativo estadoOperativo,
        boolean activo
) {
}

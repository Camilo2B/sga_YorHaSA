package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;

import java.util.List;

/** Respuesta de POST /api/apartamentos (201) y GET /api/apartamentos/{identificacion} (200). */
public record ApartamentoResponse(
        String identificacion,
        String nombre,
        int capacidad,
        List<DormitorioResponse> dormitorios,
        EstadoOperativo estadoOperativo,
        boolean activo
) {
}

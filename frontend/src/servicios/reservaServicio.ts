import type { RespuestaPaginada } from '../modelos/paginacion';
import { TAMANO_PAGINA } from '../modelos/paginacion';
import type { CrearReservaRequest, EstadoReserva, ReservaDetalle, ReservaResumen } from '../modelos/reserva';
import { api } from './api';

// POST /api/reservas → 201 Created con ReservaDetalleResponse (Guía 08)
export async function crearReserva(datos: CrearReservaRequest): Promise<ReservaDetalle> {
    const respuesta = await api.post<ReservaDetalle>('/reservas', datos);
    return respuesta.data;
}

export interface ConsultaReservas {
    estado?: EstadoReserva;
    titular?: string;
    pagina: number;
}

// GET /api/reservas?estado=…&titular=…&pagina=0&tamano=10
// Axios omite los parámetros undefined: un filtro vacío no viaja en la URL.
// signal permite cancelar la petición si ya no se necesita (AbortController).
export async function listarReservas(
    consulta: ConsultaReservas,
    signal?: AbortSignal,
): Promise<RespuestaPaginada<ReservaResumen>> {
    const respuesta = await api.get<RespuestaPaginada<ReservaResumen>>('/reservas', {
        params: { ...consulta, tamano: TAMANO_PAGINA },
        signal,
    });
    return respuesta.data;
}

// PUT /api/reservas/{codigo}/confirmar — sin cuerpo (Guía 07, 4.2)
export async function confirmarReserva(codigo: string): Promise<ReservaDetalle> {
    const respuesta = await api.put<ReservaDetalle>(`/reservas/${codigo}/confirmar`);
    return respuesta.data;
}

// PUT /api/reservas/{codigo}/cancelar — con CancelarReservaRequest { motivo }
export async function cancelarReserva(codigo: string, motivo: string): Promise<ReservaDetalle> {
    const respuesta = await api.put<ReservaDetalle>(`/reservas/${codigo}/cancelar`, { motivo });
    return respuesta.data;
}

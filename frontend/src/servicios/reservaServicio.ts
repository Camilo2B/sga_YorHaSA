import type { RespuestaPaginada } from '../modelos/paginacion';
import { TAMANO_PAGINA } from '../modelos/paginacion';
import type { CrearReservaRequest, EstadoReserva, ReservaDetalle, ReservaResumen } from '../modelos/reserva';
import { api } from './api';

// POST /api/reservas → 201 Created con ReservaDetalleResponse (Guía 08)
export async function crearReserva(datos: CrearReservaRequest): Promise<ReservaDetalle> {
    const respuesta = await api.post<ReservaDetalle>('/reservas', datos);
    return respuesta.data;
}

// GET /api/reservas?estado=PENDIENTE&pagina=0&tamano=10 (Guía 08, Ejercicio 2)
export async function listarReservas(
    estado: EstadoReserva,
    pagina: number,
): Promise<RespuestaPaginada<ReservaResumen>> {
    const respuesta = await api.get<RespuestaPaginada<ReservaResumen>>('/reservas', {
        params: { estado, pagina, tamano: TAMANO_PAGINA },
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

import type { CrearReservaRequest, EstadoReserva, ReservaDetalle, ReservaResumen } from '../modelos/reserva';
import { api } from './api';

// El token lo agrega el interceptor de api.ts: los servicios ya no se ocupan de él.

// POST /api/reservas → 201 Created con ReservaDetalleResponse (Guía 08)
export async function crearReserva(datos: CrearReservaRequest): Promise<ReservaDetalle> {
    const respuesta = await api.post<ReservaDetalle>('/reservas', datos);
    return respuesta.data;
}

// GET /api/reservas?estado=PENDIENTE (Guía 08)
export async function listarReservas(estado: EstadoReserva): Promise<ReservaResumen[]> {
    const respuesta = await api.get<ReservaResumen[]>('/reservas', { params: { estado } });
    return respuesta.data;
}

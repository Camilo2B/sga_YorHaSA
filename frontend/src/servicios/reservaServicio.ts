import type { CrearReservaRequest, ReservaDetalle } from '../modelos/reserva';
import { api } from './api';

// POST /api/reservas → 201 Created con ReservaDetalleResponse (Guía 08).
// El token lo agrega el interceptor de api.ts.
export async function crearReserva(datos: CrearReservaRequest): Promise<ReservaDetalle> {
    const respuesta = await api.post<ReservaDetalle>('/reservas', datos);
    return respuesta.data;
}

import type { CrearReservaRequest, EstadoReserva, ReservaDetalle, ReservaResumen } from '../modelos/reserva';
import { api } from './api';
import { obtenerToken } from './sesion';

// Provisional: cada petición protegida arma su propio encabezado de autorización.
// En la Guía 18 un interceptor lo hace por todas, y esta función desaparece.
function encabezadoAutorizacion() {
    const token = obtenerToken();
    return token ? { Authorization: `Bearer ${token}` } : {};
}

// POST /api/reservas → 201 Created con ReservaDetalleResponse (Guía 08)
export async function crearReserva(datos: CrearReservaRequest): Promise<ReservaDetalle> {
    const respuesta = await api.post<ReservaDetalle>('/reservas', datos, {
        headers: encabezadoAutorizacion(),
    });
    return respuesta.data;
}

// GET /api/reservas?estado=PENDIENTE (Guía 08). Axios arma la cadena de consulta con params.
export async function listarReservas(estado: EstadoReserva): Promise<ReservaResumen[]> {
    const respuesta = await api.get<ReservaResumen[]>('/reservas', {
        params: { estado },
        headers: encabezadoAutorizacion(),
    });
    return respuesta.data;
}
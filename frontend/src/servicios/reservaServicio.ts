import type { Pagina } from '../modelos/pagina';
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

// GET /api/reservas?estado=…&titular=…&page=0&size=10
export async function listarReservas(
    consulta: ConsultaReservas,
    signal?: AbortSignal,
): Promise<RespuestaPaginada<ReservaResumen>> {
    const respuesta = await api.get<Pagina<ReservaResumen>>('/reservas', {
        params: {
            estado: consulta.estado,
            titular: consulta.titular,
            page: consulta.pagina,      // El backend espera 'page', no 'pagina'
            size: TAMANO_PAGINA,        // El backend espera 'size', no 'tamano'
        },
        signal,
    });

    const data = respuesta.data;

    // Adaptamos Pagina<T> (Spring) a RespuestaPaginada<T> (Frontend)
    return {
        contenido: data.content,
        paginaActual: data.page.number,
        totalPaginas: data.page.totalPages,
        totalElementos: data.page.totalElements,
        tamanoPagina: data.page.size,
    };
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
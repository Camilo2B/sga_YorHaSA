import type { Apartamento } from '../modelos/apartamento';
import type { Pagina } from '../modelos/pagina';
import { api } from './api';

/** Parámetros de GET /api/apartamentos/disponibles (7.5). */
export interface FiltroDisponibilidad {
    fechaEntrada: string;
    fechaSalida: string;
    adultos: number;
    ninos: number;
    conMascota: boolean;
}

/** Apartamentos disponibles para las fechas y el grupo indicados, 10 por página (página 0 = la primera). */
export async function buscarDisponibles(
    filtro: FiltroDisponibilidad,
    pagina = 0,
): Promise<Pagina<Apartamento>> {
    const respuesta = await api.get<Pagina<Apartamento>>('/apartamentos/disponibles', {
        params: { ...filtro, page: pagina },
    });
    return respuesta.data;
}

export async function obtenerApartamento(identificacion: string): Promise<Apartamento> {
    const respuesta = await api.get<Apartamento>(`/apartamentos/${encodeURIComponent(identificacion)}`);
    return respuesta.data;
}

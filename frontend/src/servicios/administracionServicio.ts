import type { Apartamento } from '../modelos/apartamento';
import type { Pagina } from '../modelos/pagina';
import { api } from './api';

export interface BloqueoApartamento {
    id: string;
    identificacionApartamento: string;
    fechaInicio: string;
    fechaFin: string;
    motivo: string;
}

export async function listarApartamentos(): Promise<Apartamento[]> {
    const respuesta = await api.get<Pagina<Apartamento>>('/apartamentos', {
        params: { page: 0, size: 100, sort: 'identificacion,asc' },
    });
    return respuesta.data.content;
}

export async function listarBloqueos(identificacion: string): Promise<BloqueoApartamento[]> {
    const respuesta = await api.get<Pagina<BloqueoApartamento>>(
        `/apartamentos/${encodeURIComponent(identificacion)}/bloqueos`,
        { params: { page: 0, size: 100, sort: 'fechaInicio,asc' } },
    );
    return respuesta.data.content;
}

export async function bloquearApartamento(
    identificacion: string,
    datos: Pick<BloqueoApartamento, 'fechaInicio' | 'fechaFin' | 'motivo'>,
): Promise<BloqueoApartamento> {
    const respuesta = await api.post<BloqueoApartamento>(
        `/apartamentos/${encodeURIComponent(identificacion)}/bloquear`, datos,
    );
    return respuesta.data;
}

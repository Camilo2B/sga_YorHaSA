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

export type RolInterno = 'RECEPCIONISTA' | 'PERSONAL_SERVICIO';

export interface UsuarioInterno {
    id: number;
    nombre: string;
    correo: string;
    telefono: string;
    documento: string;
    fechaNacimiento: string;
    rol: RolInterno;
    activo: boolean;
}

export interface CrearUsuarioInternoRequest {
    nombre: string;
    correo: string;
    contrasena: string;
    telefono: string;
    documento: string;
    fechaNacimiento: string;
    rol: RolInterno;
}

export async function listarUsuariosInternos(): Promise<UsuarioInterno[]> {
    const respuesta = await api.get<UsuarioInterno[]>('/auth/usuarios-internos');
    return respuesta.data;
}

export async function crearUsuarioInterno(datos: CrearUsuarioInternoRequest): Promise<UsuarioInterno> {
    const respuesta = await api.post<UsuarioInterno>('/auth/usuarios-internos', datos);
    return respuesta.data;
}

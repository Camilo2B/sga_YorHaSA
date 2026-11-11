// src/servicios/authServicio.ts
import type { LoginRequest, TokenResponse } from '../modelos/auth';
import { api } from './api';

export interface RegistroRequest {
    nombre: string;
    correo: string;
    contrasena: string;
    telefono: string;
    documento: string;
    fechaNacimiento: string;
}

/** Petición de inicio de sesión (Guía 12) */
export async function login(datos: LoginRequest): Promise<TokenResponse> {
    const respuesta = await api.post<TokenResponse>('/auth/login', datos);
    return respuesta.data;
}

/** Petición de registro de huésped */
export async function registrar(datos: RegistroRequest): Promise<void> {
    await api.post('/auth/registro', datos);
}
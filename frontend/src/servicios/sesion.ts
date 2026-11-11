import type { TokenResponse } from '../modelos/auth';

// Una sola clave para toda la sesión: token, tipo, expiración y roles.
const CLAVE_SESION = 'sga.sesion';

export function guardarSesion(sesion: TokenResponse): void {
    localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
}

export function leerSesion(): TokenResponse | null {
    const texto = localStorage.getItem(CLAVE_SESION);
    return texto ? (JSON.parse(texto) as TokenResponse) : null;
}

export function obtenerToken(): string | null {
    return leerSesion()?.token ?? null;
}

export function borrarSesion(): void {
    localStorage.removeItem(CLAVE_SESION);
}
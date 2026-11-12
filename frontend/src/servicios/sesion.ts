import type { TokenResponse } from '../modelos/auth';

// Una sola clave para toda la sesión: token, tipo, expiración y roles.
const CLAVE_SESION = 'sga.sesion';

// Los componentes que muestran la sesión (el encabezado, por ejemplo) se suscriben aquí
// para enterarse cuando alguien inicia o cierra sesión.
const oyentes = new Set<() => void>();

function avisar(): void {
    oyentes.forEach((oyente) => oyente());
}

/**
 * Para useSyncExternalStore (hooks/useSesion.ts). Además del aviso interno, escucha el evento "storage",
 * que el navegador dispara cuando OTRA pestaña inicia o cierra sesión.
 */
export function suscribirseASesion(oyente: () => void): () => void {
    oyentes.add(oyente);
    window.addEventListener('storage', oyente);
    return () => {
        oyentes.delete(oyente);
        window.removeEventListener('storage', oyente);
    };
}

/** El texto guardado tal cual (sin interpretar): al ser un string, React compara por valor y no re-renderiza de más. */
export function leerSesionCruda(): string | null {
    return localStorage.getItem(CLAVE_SESION);
}

/** Convierte el texto guardado en una sesión, o null si no hay, está dañado o ya venció. No modifica nada. */
export function interpretarSesion(texto: string | null): TokenResponse | null {
    if (!texto) {
        return null;
    }
    try {
        const sesion = JSON.parse(texto) as TokenResponse;
        const expira = Date.parse(sesion.expiraEn);
        return Number.isNaN(expira) || expira <= Date.now() ? null : sesion;
    } catch {
        return null;
    }
}

export function guardarSesion(sesion: TokenResponse): void {
    localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
    avisar();
}

/**
 * Devuelve la sesión guardada, o null si no hay, está dañada o ya venció.
 * Una sesión inválida se borra aquí mismo: así ningún código vuelve a usar un token muerto.
 */
export function leerSesion(): TokenResponse | null {
    const texto = leerSesionCruda();
    const sesion = interpretarSesion(texto);
    if (!sesion && texto) {
        borrarSesion();
    }
    return sesion;
}

export function obtenerToken(): string | null {
    return leerSesion()?.token ?? null;
}

export function borrarSesion(): void {
    localStorage.removeItem(CLAVE_SESION);
    avisar();
}

/**
 * Lee el payload de un JWT.
 * Decodificar NO es validar: cualquiera puede leer un JWT. Quien comprueba la
 * firma y la expiración es el backend (Guía 12). El frontend solo lo usa para mostrar.
 */
function leerPayload(token: string): { sub?: string; documento?: string } | null {
    try {
        const payload = token.split('.')[1];
        // Base64URL → Base64, y los bytes se decodifican como UTF-8 (tildes y eñes)
        const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
        const bytes = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0));
        return JSON.parse(new TextDecoder().decode(bytes)) as { sub?: string; documento?: string };
    } catch {
        return null;
    }
}

/** El "sub" (el correo) del token. */
export function correoDelToken(token: string): string | null {
    return leerPayload(token)?.sub ?? null;
}

/** El "documento" del usuario: es el que el backend compara con el del titular de una reserva (7.2). */
export function documentoDelToken(token: string): string | null {
    return leerPayload(token)?.documento ?? null;
}

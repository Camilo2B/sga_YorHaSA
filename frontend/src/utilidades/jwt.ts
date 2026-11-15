/**
 * Lee el "sub" (el correo) del payload de un JWT.
 * Decodificar NO es validar: cualquiera puede leer un JWT. Quien comprueba la
 * firma y la expiración es el backend (Guía 12). El frontend solo lo usa para mostrar.
 */
export function correoDelToken(token: string): string | null {
    try {
        const payload = token.split('.')[1];
        // Base64URL → Base64, y los bytes se decodifican como UTF-8 (tildes y eñes)
        const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
        const bytes = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0));
        const datos = JSON.parse(new TextDecoder().decode(bytes)) as { sub?: string };
        return datos.sub ?? null;
    } catch {
        return null;
    }
}

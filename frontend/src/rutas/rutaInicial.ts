import type { Rol } from '../modelos/auth';

/** Página a la que se envía a cada usuario después de ingresar. */
export function rutaInicial(roles: Rol[]): string {
    if (roles.includes('ADMINISTRADOR') || roles.includes('RECEPCIONISTA')) {
        return '/reservas';
    }
    return '/';
}

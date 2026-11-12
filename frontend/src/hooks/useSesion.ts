import { useMemo, useSyncExternalStore } from 'react';
import type { Rol } from '../modelos/auth';
import { interpretarSesion, leerSesionCruda, suscribirseASesion } from '../servicios/sesion';

/**
 * La sesión actual (o null) y utilidades de rol. El componente se vuelve a dibujar solo cuando alguien
 * inicia o cierra sesión, en esta pestaña o en otra.
 */
export function useSesion() {
    const cruda = useSyncExternalStore(suscribirseASesion, leerSesionCruda);
    const sesion = useMemo(() => interpretarSesion(cruda), [cruda]);

    return {
        sesion,
        autenticado: sesion !== null,
        /** true si la sesión tiene al menos uno de esos roles. */
        tieneRol: (...roles: Rol[]) => sesion?.roles.some((rol) => roles.includes(rol)) ?? false,
    };
}

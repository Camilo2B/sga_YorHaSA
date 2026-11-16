import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { LoginRequest, Rol, TokenResponse } from '../modelos/auth';
import { registrarManejadorNoAutorizado } from '../servicios/api';
import { login } from '../servicios/authServicio';
import { borrarSesion, guardarSesion, sesionVigente } from '../servicios/sesion';
import { correoDelToken, documentoDelToken } from '../utilidades/jwt';
import { notificarAviso } from '../utilidades/notificaciones';
import { ContextoAuth, type Sesion, type ValorAuth } from './contextoAuth';


function aSesion(respuesta: TokenResponse): Sesion {
    return {
        token: respuesta.token,
        roles: respuesta.roles,
        correo: correoDelToken(respuesta.token),
        documento: documentoDelToken(respuesta.token),
        expiraEn: respuesta.expiraEn,
    };
}


function sesionInicial(): Sesion | null {
    const guardada = sesionVigente();   // una sesión con el token vencido no se restaura
    return guardada ? aSesion(guardada) : null;
}


export function AuthProvider({ children }: { children: ReactNode }) {
    // La función se ejecuta una sola vez: lee localStorage al arrancar la aplicación
    const [sesion, setSesion] = useState<Sesion | null>(sesionInicial);


    const iniciarSesion = useCallback(async (credenciales: LoginRequest) => {
        const respuesta = await login(credenciales);
        guardarSesion(respuesta);          // para sobrevivir a una recarga de la página
        const nueva = aSesion(respuesta);
        setSesion(nueva);                  // para que React vuelva a pintar quien dependa de la sesión
        return nueva;
    }, []);


    const cerrarSesion = useCallback(() => {
        borrarSesion();
        setSesion(null);
    }, []);


    // El interceptor de api.ts llama a esta función cuando el backend responde 401
    useEffect(() => {
        return registrarManejadorNoAutorizado(() => {
            borrarSesion();
            setSesion(null);
            notificarAviso('Su sesión expiró. Ingrese nuevamente.');
        });
    }, []);


    // useMemo: el objeto solo cambia cuando cambia la sesión.
    // Sin él, cada render del proveedor obligaría a re-pintar a todos los consumidores.
    const valor = useMemo<ValorAuth>(() => ({
        sesion,
        autenticado: sesion !== null,
        iniciarSesion,
        cerrarSesion,
        tieneRol: (...roles: Rol[]) => roles.some((rol) => sesion?.roles.includes(rol) ?? false),
    }), [sesion, iniciarSesion, cerrarSesion]);


    // React 19: el contexto se usa directamente como proveedor (antes, <ContextoAuth.Provider>)
    return <ContextoAuth value={valor}>{children}</ContextoAuth>;
}

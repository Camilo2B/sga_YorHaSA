import { createContext } from 'react';
import type { LoginRequest, Rol } from '../modelos/auth';

export interface Sesion {
    token: string;
    roles: Rol[];
    correo: string | null;
    expiraEn: string;
}

// Lo que el contexto pone a disposición de cualquier componente
export interface ValorAuth {
    sesion: Sesion | null;
    autenticado: boolean;
    iniciarSesion: (credenciales: LoginRequest) => Promise<Sesion>;
    cerrarSesion: () => void;
    tieneRol: (...roles: Rol[]) => boolean;
}

// null como valor inicial: así useAuth puede detectar si se usa fuera del proveedor
export const ContextoAuth = createContext<ValorAuth | null>(null);

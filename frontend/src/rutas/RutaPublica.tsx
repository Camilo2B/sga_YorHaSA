import { Navigate, Outlet, useLocation } from 'react-router';
import { useAuth } from '../hooks/useAuth';
import { rutaInicial } from './rutaInicial';


/**
 * Rutas solo para quien NO ha ingresado (login, registro). Lógica inversa a RutaProtegida.
 * También decide a dónde ir al ingresar: en cuanto la sesión existe, este componente
 * se vuelve a pintar y redirige. El login no necesita navegar por su cuenta.
 */
export function RutaPublica() {
    const { sesion } = useAuth();
    const ubicacion = useLocation();


    if (sesion) {
        // Si RutaProtegida envió al usuario aquí, se vuelve a la página que intentaba abrir
        const desde = (ubicacion.state as { desde?: string } | null)?.desde;
        return <Navigate to={desde ?? rutaInicial(sesion.roles)} replace />;
    }
    return <Outlet />;
}

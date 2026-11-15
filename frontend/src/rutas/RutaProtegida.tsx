import { Navigate, Outlet, useLocation } from 'react-router';
import { useAuth } from '../hooks/useAuth';
import type { Rol } from '../modelos/auth';


interface RutaProtegidaProps {
    roles?: Rol[];   // si no se indica, basta con estar autenticado
}


/**
 * Ruta de diseño que protege a todas sus rutas hijas.
 * Es una ayuda de navegación, NO la seguridad del sistema: quien decide qué
 * puede hacer cada rol es el backend (Guía 12). Esto solo evita mostrar
 * pantallas que de todas formas responderían 401 o 403.
 */
export function RutaProtegida({ roles }: RutaProtegidaProps) {
    const { autenticado, tieneRol } = useAuth();
    const ubicacion = useLocation();


    if (!autenticado) {
        // replace: /reservas no queda en el historial; "Atrás" no devuelve a una página bloqueada.
        // state: el login sabrá a dónde volver después de ingresar.
        return <Navigate to="/login" replace state={{ desde: ubicacion.pathname }} />;
    }
    if (roles && !tieneRol(...roles)) {
        return <Navigate to="/no-autorizado" replace />;
    }
    return <Outlet />;
}

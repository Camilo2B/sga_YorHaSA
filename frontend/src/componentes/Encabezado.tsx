import { Link, useNavigate } from 'react-router';
import { useSesion } from '../hooks/useSesion';
import { borrarSesion } from '../servicios/sesion';


interface EncabezadoProps {
    nombreAlojamiento: string;
}


export function Encabezado({ nombreAlojamiento }: EncabezadoProps) {
    const navigate = useNavigate();
    const { sesion, autenticado, tieneRol } = useSesion();

    // Mismos roles que aceptan en el backend POST /api/cotizaciones y POST /api/reservas (@PreAuthorize)
    const puedeCotizarYReservar = tieneRol('HUESPED', 'RECEPCIONISTA', 'ADMINISTRADOR');

    function cerrarSesion() {
        borrarSesion();
        navigate('/login');
    }

    return (
        <header>
            <p>{nombreAlojamiento}</p>
            <nav>
                <ul>
                    <li><Link to="/">Inicio</Link></li>
                    {puedeCotizarYReservar && <li><Link to="/cotizaciones">Cotizar</Link></li>}
                    {puedeCotizarYReservar && <li><Link to="/reservas/nueva">Nueva reserva</Link></li>}
                    {!autenticado && <li><Link to="/login">Ingresar</Link></li>}
                    {!autenticado && <li><Link to="/registro">Registrarse</Link></li>}
                    {sesion && (
                        <>
                            <li>Sesión: {sesion.roles.join(', ')}</li>
                            <li><button type="button" onClick={cerrarSesion}>Cerrar sesión</button></li>
                        </>
                    )}
                </ul>
            </nav>
        </header>
    );
}

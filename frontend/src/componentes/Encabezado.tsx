import { useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router';
import { borrarSesion, leerSesion } from '../servicios/sesion';


interface EncabezadoProps {
    nombreAlojamiento: string;
}


// NavLink entrega isActive: se usa para marcar la opción de la página actual.
function claseEnlace({ isActive }: { isActive: boolean }) {
    return `nav-link ${isActive ? 'active fw-semibold' : ''}`;
}


export function Encabezado({ nombreAlojamiento }: EncabezadoProps) {
    const [menuAbierto, setMenuAbierto] = useState(false);
    const navigate = useNavigate();


    // Provisional: useLocation() hace que el encabezado se vuelva a pintar en cada
    // navegación y así relea la sesión. En la Guía 18 la sesión vive en un contexto.
    useLocation();
    const sesion = leerSesion();


    function cerrarSesion() {
        borrarSesion();
        navigate('/login');
    }


    return (
        <header>
            <nav className="navbar navbar-expand-md bg-body-tertiary border-bottom">
                <div className="container">
                    <Link to="/" id="marca" className="navbar-brand d-flex align-items-center gap-2">
                        <i className="bi bi-house-heart" aria-hidden="true"></i>
                        {nombreAlojamiento}
                    </Link>


                    {/* Botón hamburguesa: solo se ve por debajo del punto de quiebre md */}
                    <button
                        className="navbar-toggler"
                        type="button"
                        aria-controls="menuPrincipal"
                        aria-expanded={menuAbierto}
                        aria-label="Abrir menú"
                        onClick={() => setMenuAbierto(!menuAbierto)}
                    >
                        <span className="navbar-toggler-icon"></span>
                    </button>


                    {/* Clase condicional: "show" despliega el menú en pantallas pequeñas */}
                    <div
                        id="menuPrincipal"
                        className={`collapse navbar-collapse ${menuAbierto ? 'show' : ''}`}
                        onClick={() => setMenuAbierto(false)}
                    >
                        <ul className="navbar-nav me-auto">
                            <li className="nav-item">
                                <NavLink to="/" end className={claseEnlace}>Inicio</NavLink>
                            </li>
                            {sesion && (
                                <>
                                    <li className="nav-item">
                                        <NavLink to="/reservas" end className={claseEnlace}>Reservas</NavLink>
                                    </li>
                                    <li className="nav-item">
                                        <NavLink to="/reservas/nueva" className={claseEnlace}>Nueva reserva</NavLink>
                                    </li>
                                </>
                            )}
                        </ul>


                        {sesion ? (
                            <button type="button" className="btn btn-outline-danger btn-sm" onClick={cerrarSesion}>
                                <i className="bi bi-box-arrow-right me-1" aria-hidden="true"></i>Cerrar sesión
                            </button>
                        ) : (
                            <div className="d-flex gap-2">
                                <Link to="/login" className="btn btn-primary btn-sm">Ingresar</Link>
                                <Link to="/registro" className="btn btn-outline-secondary btn-sm">Registrarse</Link>
                            </div>
                        )}
                    </div>
                </div>
            </nav>
        </header>
    );
}

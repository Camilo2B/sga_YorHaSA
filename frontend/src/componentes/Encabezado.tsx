import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router';
import { useAuth } from '../hooks/useAuth';


interface EncabezadoProps {
    nombreAlojamiento: string;
}


function claseEnlace({ isActive }: { isActive: boolean }) {
    return `nav-link ${isActive ? 'active fw-semibold' : ''}`;
}


export function Encabezado({ nombreAlojamiento }: EncabezadoProps) {
    const [menuAbierto, setMenuAbierto] = useState(false);
    const navigate = useNavigate();
    // La sesión ya no se lee de localStorage: viene del contexto, y cualquier
    // cambio (login, logout, 401) vuelve a pintar el encabezado automáticamente.
    const { sesion, tieneRol, cerrarSesion } = useAuth();


    function salir() {
        cerrarSesion();
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


                    <div
                        id="menuPrincipal"
                        className={`collapse navbar-collapse ${menuAbierto ? 'show' : ''}`}
                        onClick={() => setMenuAbierto(false)}
                    >
                        <ul className="navbar-nav me-auto">
                            <li className="nav-item">
                                <NavLink to="/" end className={claseEnlace}>Inicio</NavLink>
                            </li>
                            {/* Menú según el rol: cada opción aparece solo para quien puede usarla */}
                            {tieneRol('RECEPCIONISTA', 'ADMINISTRADOR') && (
                                <>
                                    <li className="nav-item">
                                        <NavLink to="/reservas" end className={claseEnlace}>Reservas</NavLink>
                                    </li>
                                    <li className="nav-item">
                                        <NavLink to="/reservas/nueva" className={claseEnlace}>Nueva reserva</NavLink>
                                    </li>
                                </>
                            )}
                            {tieneRol('HUESPED') && (
                                <>
                                    <li className="nav-item">
                                        <NavLink to="/reservas/nueva" className={claseEnlace}>Reservar</NavLink>
                                    </li>
                                    <li className="nav-item">
                                        <NavLink to="/mis-reservas" className={claseEnlace}>Mis reservas</NavLink>
                                    </li>
                                </>
                            )}
                            {tieneRol('ADMINISTRADOR') && (
                                <li className="nav-item">
                                    <NavLink to="/administracion" className={claseEnlace}>Administración</NavLink>
                                </li>
                            )}
                            {tieneRol('RECEPCIONISTA', 'PERSONAL_SERVICIO') && (
                                <li className="nav-item">
                                    <NavLink to="/gestion-apartamentos" className={claseEnlace}>Apartamentos</NavLink>
                                </li>
                            )}
                        </ul>


                        {sesion ? (
                            <div className="d-flex align-items-center gap-2">
                                <span className="navbar-text small">{sesion.correo}</span>
                                <button type="button" className="btn btn-outline-danger btn-sm" onClick={salir}>
                                    <i className="bi bi-box-arrow-right me-1" aria-hidden="true"></i>Cerrar sesión
                                </button>
                            </div>
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

import { Outlet } from 'react-router';
import { Encabezado } from './Encabezado';
import { PiePagina } from './PiePagina';


// Estructura común a todas las páginas. <Outlet /> es el hueco donde
// React Router pinta la página de la ruta hija que esté activa.
export function Layout() {
    return (
        <>
            <Encabezado nombreAlojamiento="Bunker Orbital YorHa S.A" />
            <main className="container py-4">
                <Outlet />
            </main>
            <PiePagina />
        </>
    );
}

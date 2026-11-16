import { Suspense } from 'react';
import { Outlet } from 'react-router';
import { Cargando } from './Cargando';
import { Encabezado } from './Encabezado';
import { PiePagina } from './PiePagina';


export function Layout() {
    return (
        <>
            <Encabezado nombreAlojamiento="Bunker Orbital YorHa S.A" />
            <main className="container py-4">
                {/* Mientras se descarga el código de una página diferida (lazy), se muestra
            el indicador. El encabezado y el pie no desaparecen: están fuera del Suspense. */}
                <Suspense fallback={<Cargando />}>
                    <Outlet />
                </Suspense>
            </main>
            <PiePagina />
        </>
    );
}

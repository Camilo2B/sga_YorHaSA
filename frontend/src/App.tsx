import { lazy } from 'react';
import { Route, Routes } from 'react-router';
import { Layout } from './componentes/Layout';
import { FiltrosProvider } from './contexto/FiltrosProvider';
import { useAuth } from './hooks/useAuth';
import Inicio from './paginas/Inicio';
import { RutaProtegida } from './rutas/RutaProtegida';
import { RutaPublica } from './rutas/RutaPublica';

// Carga diferida: el código de cada página se descarga la primera vez que se visita.
// lazy() necesita una importación dinámica de un módulo con export default.
const SubirImagenes = lazy(() => import('./paginas/SubirImagenes'));
const DetalleApartamento = lazy(() => import('./paginas/DetalleApartamento'));
const ListaReservas = lazy(() => import('./paginas/ListaReservas'));
const MisReservas = lazy(() => import('./paginas/MisReservas'));
const Login = lazy(() => import('./paginas/Login'));
const NoAutorizado = lazy(() => import('./paginas/NoAutorizado'));
const NoEncontrada = lazy(() => import('./paginas/NoEncontrada'));
const NuevaReserva = lazy(() => import('./paginas/NuevaReserva'));
const Registro = lazy(() => import('./paginas/Registro'));


export default function App() {
    const { sesion } = useAuth();


    return (
        // key: si cambia el usuario (login, logout, sesión expirada), React descarta este
        // proveedor y crea uno nuevo. Los filtros de un usuario no le quedan al siguiente.
        <FiltrosProvider key={sesion?.correo ?? 'anonimo'}>
            <Routes>
                <Route element={<Layout />}>
                    {/* Inicio se carga de inmediato: es la primera página que ve casi todo el mundo */}
                    <Route index element={<Inicio />} />
                    <Route path="apartamentos/:identificacion" element={<DetalleApartamento />} />
                    <Route path="no-autorizado" element={<NoAutorizado />} />


                    <Route element={<RutaPublica />}>
                        <Route path="login" element={<Login />} />
                        <Route path="registro" element={<Registro />} />
                    </Route>


                    {/* Reservar: el huésped lo hace desde el portal; recepción, a nombre de quien llega */}
                    <Route element={<RutaProtegida roles={['HUESPED', 'RECEPCIONISTA', 'ADMINISTRADOR']} />}>
                        <Route path="reservas/nueva" element={<NuevaReserva />} />
                    </Route>


                    <Route element={<RutaProtegida roles={['RECEPCIONISTA', 'ADMINISTRADOR']} />}>
                        <Route path="reservas" element={<ListaReservas />} />
                    </Route>


                    <Route element={<RutaProtegida roles={['HUESPED']} />}>
                        <Route path="mis-reservas" element={<MisReservas />} />
                    </Route>


                    <Route element={<RutaProtegida roles={['ADMINISTRADOR']} />}>
                        <Route path="administracion/apartamentos/:identificacion/imagenes" element={<SubirImagenes />} />
                    </Route>


                    <Route path="*" element={<NoEncontrada />} />
                </Route>
            </Routes>
        </FiltrosProvider>
    );
}

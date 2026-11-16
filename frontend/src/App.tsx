import { lazy } from 'react';
import { Route, Routes } from 'react-router';
import { Layout } from './componentes/Layout';
import Inicio from './paginas/Inicio';
import { RutaProtegida } from './rutas/RutaProtegida';
import { RutaPublica } from './rutas/RutaPublica';


// Carga diferida: el código de cada página se descarga la primera vez que se visita.
// lazy() necesita una importación dinámica de un módulo con export default.
const Administracion = lazy(() => import('./paginas/Administracion'));
const DetalleApartamento = lazy(() => import('./paginas/DetalleApartamento'));
const ListaReservas = lazy(() => import('./paginas/ListaReservas'));
const Login = lazy(() => import('./paginas/Login'));
const NoAutorizado = lazy(() => import('./paginas/NoAutorizado'));
const NoEncontrada = lazy(() => import('./paginas/NoEncontrada'));
const NuevaReserva = lazy(() => import('./paginas/NuevaReserva'));
const Registro = lazy(() => import('./paginas/Registro'));


export default function App() {
    return (
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


                <Route element={<RutaProtegida roles={['RECEPCIONISTA', 'ADMINISTRADOR']} />}>
                    <Route path="reservas" element={<ListaReservas />} />
                    <Route path="reservas/nueva" element={<NuevaReserva />} />
                </Route>


                <Route element={<RutaProtegida roles={['ADMINISTRADOR']} />}>
                    <Route path="administracion" element={<Administracion />} />
                </Route>


                <Route path="*" element={<NoEncontrada />} />
            </Route>
        </Routes>
    );
}

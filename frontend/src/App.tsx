import { Route, Routes } from 'react-router';
import { Layout } from './componentes/Layout';
import Administracion from './paginas/Administracion';
import DetalleApartamento from './paginas/DetalleApartamento';
import Inicio from './paginas/Inicio';
import ListaReservas from './paginas/ListaReservas';
import Login from './paginas/Login';
import NoAutorizado from './paginas/NoAutorizado';
import NoEncontrada from './paginas/NoEncontrada';
import NuevaReserva from './paginas/NuevaReserva';
import Registro from './paginas/Registro';
import { RutaProtegida } from './rutas/RutaProtegida';
import { RutaPublica } from './rutas/RutaPublica';


export default function App() {
    return (
        <Routes>
            <Route element={<Layout />}>
                {/* Públicas para todos */}
                <Route index element={<Inicio />} />
                <Route path="apartamentos/:identificacion" element={<DetalleApartamento />} />
                <Route path="no-autorizado" element={<NoAutorizado />} />


                {/* Solo sin sesión */}
                <Route element={<RutaPublica />}>
                    <Route path="login" element={<Login />} />
                    <Route path="registro" element={<Registro />} />
                </Route>


                {/* Recepción: recepcionista y administrador (7.1) */}
                <Route element={<RutaProtegida roles={['RECEPCIONISTA', 'ADMINISTRADOR']} />}>
                    <Route path="reservas" element={<ListaReservas />} />
                    <Route path="reservas/nueva" element={<NuevaReserva />} />
                </Route>


                {/* Solo administrador */}
                <Route element={<RutaProtegida roles={['ADMINISTRADOR']} />}>
                    <Route path="administracion" element={<Administracion />} />
                </Route>


                <Route path="*" element={<NoEncontrada />} />
            </Route>
        </Routes>
    );
}

import { Route, Routes } from 'react-router';
import { Layout } from './componentes/Layout';
import DetalleApartamento from './paginas/DetalleApartamento';
import Inicio from './paginas/Inicio';
import ListaReservas from './paginas/ListaReservas';
import Login from './paginas/Login';
import NoEncontrada from './paginas/NoEncontrada';
import NuevaReserva from './paginas/NuevaReserva';
import Registro from './paginas/Registro';


export default function App() {
    return (
        <Routes>
            {/* Ruta de diseño: no tiene path; todas sus hijas se pintan dentro de su <Outlet /> */}
            <Route element={<Layout />}>
                <Route index element={<Inicio />} />
                <Route path="login" element={<Login />} />
                <Route path="registro" element={<Registro />} />
                <Route path="apartamentos/:identificacion" element={<DetalleApartamento />} />
                <Route path="reservas" element={<ListaReservas />} />
                <Route path="reservas/nueva" element={<NuevaReserva />} />
                <Route path="*" element={<NoEncontrada />} />
            </Route>
        </Routes>
    );
}

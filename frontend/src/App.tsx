import { Route, Routes } from 'react-router';
import { Encabezado } from './componentes/Encabezado';
import { PiePagina } from './componentes/PiePagina';
import DetalleApartamento from './paginas/DetalleApartamento';
import Inicio from './paginas/Inicio';
import Login from './paginas/Login';
import NoEncontrada from './paginas/NoEncontrada';
import Registro from './paginas/Registro';
import NuevaReserva from './paginas/NuevaReserva';
import Cotizacion from './paginas/Cotizacion';

export default function App() {
    return (
        <>
            <Encabezado nombreAlojamiento="Búnker Orbital YoRHa S.A." />

            <main>
                <Routes>
                    <Route path="/" element={<Inicio />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/registro" element={<Registro />} />
                    <Route path="/apartamentos/:identificacion" element={<DetalleApartamento />} />
                    <Route path="/reservas/nueva" element={<NuevaReserva />} />
                    <Route path="/cotizaciones" element={<Cotizacion />} />
                    <Route path="*" element={<NoEncontrada />} />
                </Routes>
            </main>

            <PiePagina />
        </>
    );
}
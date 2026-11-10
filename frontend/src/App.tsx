import { Route, Routes } from 'react-router';
import { Encabezado } from './componentes/Encabezado';
import { PiePagina } from './componentes/PiePagina';
import DetalleApartamento from './paginas/DetalleApartamento';
import Inicio from './paginas/Inicio';
import Login from './paginas/Login';
import NoEncontrada from './paginas/NoEncontrada';
import Registro from './paginas/Registro';


export default function App() {
  return (
      <>
        <Encabezado nombreAlojamiento="Bunker Orbital YorHa S.A" />


        <main>
          <Routes>
            <Route path="/" element={<Inicio />} />
            <Route path="/login" element={<Login />} />
            <Route path="/registro" element={<Registro />} />
            <Route path="/apartamentos/:identificacion" element={<DetalleApartamento />} />
            <Route path="*" element={<NoEncontrada />} />
          </Routes>
        </main>


        <PiePagina />
      </>
  );
}

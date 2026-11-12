import { Link } from 'react-router';
import type { Apartamento } from '../modelos/apartamento';


interface TarjetaApartamentoProps {
    apartamento: Apartamento;
}


export function TarjetaApartamento({ apartamento }: TarjetaApartamentoProps) {
    return (
        <article className="tarjeta">
            <h3>{apartamento.nombre}</h3>
            <p>{apartamento.identificacion}</p>
            <p>
                {apartamento.dormitorios.length} dormitorio(s) · hasta {apartamento.capacidad} personas
                {apartamento.permiteMascotas ? ' · admite mascotas' : ''}
            </p>
            <Link to={`/apartamentos/${apartamento.identificacion}`}>Ver detalle</Link>
        </article>
    );
}

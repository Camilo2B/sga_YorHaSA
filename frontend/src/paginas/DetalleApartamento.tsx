import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import type { Apartamento } from '../modelos/apartamento';
import { obtenerApartamento } from '../servicios/apartamentoServicio';
import { mensajeDeError } from '../utilidades/errores';


export default function DetalleApartamento() {
    // El nombre del parámetro coincide con el de la ruta: /apartamentos/:identificacion
    const { identificacion } = useParams();
    const [apartamento, setApartamento] = useState<Apartamento | null>(null);
    const [mensaje, setMensaje] = useState<string | null>(null);

    useEffect(() => {
        if (!identificacion) {
            return;
        }
        let cancelado = false;
        obtenerApartamento(identificacion)
            .then((respuesta) => {
                if (!cancelado) {
                    setApartamento(respuesta);
                    setMensaje(null);
                }
            })
            .catch((error: unknown) => {
                if (!cancelado) {
                    setApartamento(null);
                    setMensaje(mensajeDeError(error));   // un 404 trae "no existe el apartamento ..."
                }
            });
        return () => {
            cancelado = true;
        };
    }, [identificacion]);

    // Si el estado guardado es de otro apartamento (se navegó entre detalles), todavía se está cargando
    const visible = apartamento?.identificacion === identificacion ? apartamento : null;

    if (mensaje) {
        return (
            <section>
                <p className="error">{mensaje}</p>
                <Link to="/">Volver</Link>
            </section>
        );
    }
    if (!visible) {
        return <p>Cargando…</p>;
    }

    const consulta = `?apartamento=${encodeURIComponent(visible.identificacion)}`;

    return (
        <section>
            <h1>{visible.nombre}</h1>
            <p>Identificación: {visible.identificacion}</p>
            <p>Capacidad: {visible.capacidad} personas</p>
            <p>Admite mascotas: {visible.permiteMascotas ? 'Sí' : 'No'}</p>
            <p>Estado: {visible.activo ? visible.estadoOperativo : 'Retirado de la venta'}</p>

            <h2>Dormitorios</h2>
            <ul>
                {visible.dormitorios.map((dormitorio, indice) => (
                    <li key={indice}>{dormitorio.descripcion}: {dormitorio.tipoCama}</li>
                ))}
            </ul>

            <p>
                <Link to={`/cotizaciones${consulta}`}>Cotizar este apartamento</Link>
                {' · '}
                <Link to={`/reservas/nueva${consulta}`}>Reservar</Link>
                {' · '}
                <Link to="/">Volver</Link>
            </p>
        </section>
    );
}

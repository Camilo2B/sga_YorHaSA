import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { TarjetaReserva } from '../componentes/TarjetaReserva';
import type { EstadoReserva, ReservaResumen } from '../modelos/reserva';
import { listarReservas } from '../servicios/reservaServicio';
import { mensajeDeError } from '../utilidades/errores';


const ESTADOS: EstadoReserva[] = [
    'PENDIENTE', 'CONFIRMADA', 'EN_CURSO', 'FINALIZADA', 'CANCELADA', 'NO_SHOW',
];


export default function ListaReservas() {
    const [estado, setEstado] = useState<EstadoReserva>('PENDIENTE');
    const [reservas, setReservas] = useState<ReservaResumen[]>([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState<string | null>(null);


    // El cambio de estado lo provoca el usuario: aquí se prepara la pantalla para la nueva consulta
    function cambiarEstado(nuevo: EstadoReserva) {
        setEstado(nuevo);
        setCargando(true);
        setError(null);
    }


    // Se ejecuta al montar la página y cada vez que cambia el estado elegido
    useEffect(() => {
        // Si el usuario cambia de estado antes de que llegue la respuesta anterior,
        // esa respuesta vieja se descarta. La Guía 20 la cancela con AbortController.
        let ignorar = false;


        listarReservas(estado)
            .then((datos) => { if (!ignorar) setReservas(datos); })
            .catch((e: unknown) => { if (!ignorar) setError(mensajeDeError(e)); })
            .finally(() => { if (!ignorar) setCargando(false); });


        return () => { ignorar = true; };
    }, [estado]);


    return (
        <section>
            <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                <h1 className="h3 mb-0">
                    <i className="bi bi-journal-text me-2" aria-hidden="true"></i>Reservas
                </h1>
                <Link to="/reservas/nueva" className="btn btn-primary">
                    <i className="bi bi-plus-lg me-1" aria-hidden="true"></i>Nueva reserva
                </Link>
            </div>


            <div className="mb-3" style={{ maxWidth: '16rem' }}>
                <label htmlFor="estado" className="form-label">Estado</label>
                <select
                    id="estado"
                    className="form-select"
                    value={estado}
                    onChange={(e) => cambiarEstado(e.target.value as EstadoReserva)}
                >
                    {ESTADOS.map((valor) => <option key={valor} value={valor}>{valor}</option>)}
                </select>
            </div>


            {cargando && (
                <div className="d-flex justify-content-center my-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Cargando…</span>
                    </div>
                </div>
            )}


            {!cargando && error && <div className="alert alert-danger">{error}</div>}


            {!cargando && !error && reservas.length === 0 && (
                <div className="text-center text-body-secondary py-5">
                    <i className="bi bi-inbox fs-1 d-block mb-2" aria-hidden="true"></i>
                    No hay reservas en estado {estado}.
                </div>
            )}


            {!cargando && !error && reservas.length > 0 && (
                <div className="row g-3">
                    {reservas.map((reserva) => (
                        <div key={reserva.codigo} className="col-12 col-md-6 col-lg-4">
                            <TarjetaReserva reserva={reserva} />
                        </div>
                    ))}
                </div>
            )}
        </section>
    );
}

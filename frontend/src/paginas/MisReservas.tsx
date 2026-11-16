import axios from 'axios';
import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { Cargando } from '../componentes/Cargando';
import { Paginacion } from '../componentes/Paginacion';
import { TarjetaReserva } from '../componentes/TarjetaReserva';
import type { RespuestaPaginada } from '../modelos/paginacion';
import type { EstadoReserva, ReservaResumen } from '../modelos/reserva';
import { cancelarReserva, listarMisReservas } from '../servicios/reservaServicio';
import { mensajeDeError } from '../utilidades/errores';
import { notificarError, notificarExito, pedirMotivo } from '../utilidades/notificaciones';


const ESTADOS: EstadoReserva[] = [
    'PENDIENTE', 'CONFIRMADA', 'EN_CURSO', 'FINALIZADA', 'CANCELADA', 'NO_SHOW',
];


/** Portal del huésped: las reservas de las que es titular (GET /api/reservas/mias). */
export default function MisReservas() {
    // Estado local: esta pantalla es solo del huésped y no comparte filtros con el listado de recepción
    const [estado, setEstado] = useState<EstadoReserva | ''>('');
    const [pagina, setPagina] = useState(0);
    const [recargas, setRecargas] = useState(0);
    const [datos, setDatos] = useState<RespuestaPaginada<ReservaResumen> | null>(null);
    const [error, setError] = useState<string | null>(null);


    // "¿Está cargando?" se DERIVA: la consulta que se quiere hacer ya no es la última respondida
    const claveConsulta = `${estado}|${pagina}|${recargas}`;
    const [claveRespondida, setClaveRespondida] = useState<string | null>(null);
    const cargando = claveRespondida !== claveConsulta;


    useEffect(() => {
        // Si el filtro cambia antes de que responda, la limpieza cancela la petición vieja
        const controlador = new AbortController();

        listarMisReservas({ estado: estado || undefined, pagina }, controlador.signal)
            .then((respuesta) => {
                setDatos(respuesta);
                setError(null);
                setClaveRespondida(claveConsulta);
            })
            .catch((e: unknown) => {
                if (axios.isCancel(e)) return;   // cancelada a propósito: no es un error
                setError(mensajeDeError(e));
                setClaveRespondida(claveConsulta);
            });

        return () => controlador.abort();
    }, [estado, pagina, claveConsulta]);


    async function cancelar(codigo: string) {
        const motivo = await pedirMotivo(`Cancelar ${codigo}`);
        if (motivo === null) return;
        try {
            await cancelarReserva(codigo, motivo);
            notificarExito(`Reserva ${codigo} cancelada`);
            setRecargas((n) => n + 1);
        } catch (e) {
            notificarError(mensajeDeError(e));
        }
    }


    const reservas = datos?.contenido ?? [];


    return (
        <section>
            <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                <h1 className="h3 mb-0">
                    <i className="bi bi-journal-bookmark me-2" aria-hidden="true"></i>Mis reservas
                </h1>
                <Link to="/reservas/nueva" className="btn btn-primary">
                    <i className="bi bi-plus-lg me-1" aria-hidden="true"></i>Nueva reserva
                </Link>
            </div>


            <div className="row g-2 align-items-end mb-3 mx-0 p-3 bg-body-tertiary rounded">
                <div className="col-12 col-md-6 col-lg-4">
                    <label htmlFor="estado" className="form-label">Estado</label>
                    <select
                        id="estado"
                        className="form-select"
                        value={estado}
                        onChange={(e) => {
                            setEstado(e.target.value as EstadoReserva | '');
                            setPagina(0);   // un filtro nuevo vuelve a la primera página
                        }}
                    >
                        <option value="">Todos</option>
                        {ESTADOS.map((valor) => <option key={valor} value={valor}>{valor}</option>)}
                    </select>
                </div>
            </div>


            {cargando && !datos && <Cargando />}

            {!cargando && error && <div className="alert alert-danger">{error}</div>}

            {datos && !error && reservas.length === 0 && !cargando && (
                <div className="text-center text-body-secondary py-5">
                    <i className="bi bi-inbox fs-1 d-block mb-2" aria-hidden="true"></i>
                    {estado ? 'No tiene reservas con ese estado.' : 'Aún no ha hecho ninguna reserva.'}
                    <div className="mt-3">
                        <Link to="/" className="btn btn-outline-primary btn-sm">Ver apartamentos</Link>
                    </div>
                </div>
            )}

            {datos && !error && reservas.length > 0 && (
                <div className={cargando ? 'opacity-50' : ''} aria-busy={cargando}>
                    <p className="text-body-secondary small">
                        {datos.totalElementos} reserva(s) · página {datos.paginaActual + 1} de {datos.totalPaginas}
                    </p>
                    <div className="row g-3 mb-3">
                        {reservas.map((reserva) => (
                            <div key={reserva.codigo} className="col-12 col-md-6 col-lg-4">
                                <TarjetaReserva
                                    reserva={reserva}
                                    acciones={
                                        (reserva.estado === 'PENDIENTE' || reserva.estado === 'CONFIRMADA') && (
                                            <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => cancelar(reserva.codigo)}>
                                                Cancelar
                                            </button>
                                        )
                                    }
                                />
                            </div>
                        ))}
                    </div>
                    <Paginacion
                        paginaActual={datos.paginaActual}
                        totalPaginas={datos.totalPaginas}
                        alCambiar={setPagina}
                    />
                </div>
            )}
        </section>
    );
}

import axios from 'axios';
import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { Cargando } from '../componentes/Cargando';
import { Paginacion } from '../componentes/Paginacion';
import { TarjetaReserva } from '../componentes/TarjetaReserva';
import { useDebounce } from '../hooks/useDebounce';
import { useFiltros } from '../hooks/useFiltros';
import type { RespuestaPaginada } from '../modelos/paginacion';
import type { EstadoReserva, ReservaResumen } from '../modelos/reserva';
import { cancelarReserva, confirmarReserva, listarReservas } from '../servicios/reservaServicio';
import { mensajeDeError } from '../utilidades/errores';
import { confirmarAccion, notificarError, notificarExito, pedirMotivo } from '../utilidades/notificaciones';


const ESTADOS: EstadoReserva[] = [
    'PENDIENTE', 'CONFIRMADA', 'EN_CURSO', 'FINALIZADA', 'CANCELADA', 'NO_SHOW',
];


export default function ListaReservas() {
    // Los filtros ya no son estado local: viven en el contexto y sobreviven a la navegación
    const { filtros, despachar } = useFiltros();
    // El texto se guarda en el contexto tecla a tecla, pero la consulta usa su versión estable
    const titularBuscado = useDebounce(filtros.titular, 400);


    const [recargas, setRecargas] = useState(0);
    const [datos, setDatos] = useState<RespuestaPaginada<ReservaResumen> | null>(null);
    const [error, setError] = useState<string | null>(null);


    // "¿Está cargando?" se DERIVA: la consulta que se quiere hacer ya no es la última respondida
    const claveConsulta = `${filtros.estado}|${titularBuscado}|${filtros.pagina}|${recargas}`;
    const [claveRespondida, setClaveRespondida] = useState<string | null>(null);
    const cargando = claveRespondida !== claveConsulta;


    useEffect(() => {
        // Un controlador por petición. Si los filtros cambian antes de que responda,
        // la limpieza la cancela: una respuesta vieja nunca pinta datos viejos.
        const controlador = new AbortController();


        listarReservas(
            {
                estado: filtros.estado || undefined,
                titular: titularBuscado.trim() || undefined,
                pagina: filtros.pagina,
            },
            controlador.signal,
        )
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
    }, [filtros.estado, titularBuscado, filtros.pagina, recargas, claveConsulta]);


    function recargar() {
        setRecargas((n) => n + 1);
    }


    async function confirmar(codigo: string) {
        if (!(await confirmarAccion('¿Confirmar la reserva?', codigo))) return;
        try {
            await confirmarReserva(codigo);
            notificarExito(`Reserva ${codigo} confirmada`);
            recargar();
        } catch (e) {
            notificarError(mensajeDeError(e));
        }
    }


    async function cancelar(codigo: string) {
        const motivo = await pedirMotivo(`Cancelar ${codigo}`);
        if (motivo === null) return;
        try {
            await cancelarReserva(codigo, motivo);
            notificarExito(`Reserva ${codigo} cancelada`);
            recargar();
        } catch (e) {
            notificarError(mensajeDeError(e));
        }
    }


    const reservas = datos?.contenido ?? [];


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


            {/* Barra de filtros */}
            <div className="row g-2 align-items-end mb-3 mx-0 p-3 bg-body-tertiary rounded">
                <div className="col-12 col-md-5">
                    <label htmlFor="titular" className="form-label">Titular</label>
                    <div className="input-group">
                        <span className="input-group-text"><i className="bi bi-search" aria-hidden="true"></i></span>
                        <input
                            id="titular"
                            type="search"
                            className="form-control"
                            placeholder="Buscar por nombre…"
                            value={filtros.titular}
                            onChange={(e) => despachar({ tipo: 'cambiarTitular', titular: e.target.value })}
                        />
                    </div>
                </div>
                <div className="col-12 col-md-4">
                    <label htmlFor="estado" className="form-label">Estado</label>
                    <select
                        id="estado"
                        className="form-select"
                        value={filtros.estado}
                        onChange={(e) => despachar({ tipo: 'cambiarEstado', estado: e.target.value as EstadoReserva | '' })}
                    >
                        <option value="">Todos</option>
                        {ESTADOS.map((valor) => <option key={valor} value={valor}>{valor}</option>)}
                    </select>
                </div>
                <div className="col-12 col-md-3 d-grid">
                    <button type="button" className="btn btn-outline-secondary" onClick={() => despachar({ tipo: 'limpiar' })}>
                        <i className="bi bi-x-circle me-1" aria-hidden="true"></i>Limpiar filtros
                    </button>
                </div>
            </div>


            {/* Primera carga: aún no hay nada que mostrar */}
            {cargando && !datos && <Cargando />}


            {!cargando && error && <div className="alert alert-danger">{error}</div>}


            {datos && !error && reservas.length === 0 && !cargando && (
                <div className="text-center text-body-secondary py-5">
                    <i className="bi bi-inbox fs-1 d-block mb-2" aria-hidden="true"></i>
                    No hay reservas con esos filtros.
                </div>
            )}


            {datos && !error && reservas.length > 0 && (
                // Mientras llega la nueva página, la anterior se atenúa en lugar de desaparecer
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
                                        <>
                                            {reserva.estado === 'PENDIENTE' && (
                                                <button type="button" className="btn btn-sm btn-success" onClick={() => confirmar(reserva.codigo)}>
                                                    Confirmar
                                                </button>
                                            )}
                                            {(reserva.estado === 'PENDIENTE' || reserva.estado === 'CONFIRMADA') && (
                                                <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => cancelar(reserva.codigo)}>
                                                    Cancelar
                                                </button>
                                            )}
                                        </>
                                    }
                                />
                            </div>
                        ))}
                    </div>
                    <Paginacion
                        paginaActual={datos.paginaActual}
                        totalPaginas={datos.totalPaginas}
                        alCambiar={(pagina) => despachar({ tipo: 'cambiarPagina', pagina })}
                    />
                </div>
            )}
        </section>
    );
}

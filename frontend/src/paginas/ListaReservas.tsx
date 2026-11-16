import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { Cargando } from '../componentes/Cargando';
import { Paginacion } from '../componentes/Paginacion';
import { TarjetaReserva } from '../componentes/TarjetaReserva';
import type { RespuestaPaginada } from '../modelos/paginacion';
import type { EstadoReserva, ReservaResumen } from '../modelos/reserva';
import { cancelarReserva, confirmarReserva, listarReservas } from '../servicios/reservaServicio';
import { mensajeDeError } from '../utilidades/errores';
import { confirmarAccion, notificarError, notificarExito, pedirMotivo } from '../utilidades/notificaciones';


const ESTADOS: EstadoReserva[] = [
    'PENDIENTE', 'CONFIRMADA', 'EN_CURSO', 'FINALIZADA', 'CANCELADA', 'NO_SHOW',
];


export default function ListaReservas() {
    const [estado, setEstado] = useState<EstadoReserva>('PENDIENTE');
    const [pagina, setPagina] = useState(0);
    const [recargas, setRecargas] = useState(0);   // cambiarlo fuerza una nueva consulta
    const [datos, setDatos] = useState<RespuestaPaginada<ReservaResumen> | null>(null);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState<string | null>(null);


    function prepararConsulta() {
        setCargando(true);
        setError(null);
    }


    function cambiarEstado(nuevo: EstadoReserva) {
        setEstado(nuevo);
        setPagina(0);   // un filtro nuevo siempre empieza en la primera página
        prepararConsulta();
    }


    function cambiarPagina(nueva: number) {
        setPagina(nueva);
        prepararConsulta();
    }


    function recargar() {
        setRecargas((n) => n + 1);
        prepararConsulta();
    }


    // Paginación del servidor: cada página es una petición con ?pagina=N
    useEffect(() => {
        let ignorar = false;
        listarReservas(estado, pagina)
            .then((respuesta) => { if (!ignorar) setDatos(respuesta); })
            .catch((e: unknown) => { if (!ignorar) setError(mensajeDeError(e)); })
            .finally(() => { if (!ignorar) setCargando(false); });
        return () => { ignorar = true; };
    }, [estado, pagina, recargas]);


    async function confirmar(codigo: string) {
        if (!(await confirmarAccion('¿Confirmar la reserva?', codigo))) return;
        try {
            await confirmarReserva(codigo);
            notificarExito(`Reserva ${codigo} confirmada`);
            recargar();
        } catch (e) {
            notificarError(mensajeDeError(e));   // p. ej., 409: falta la hora estimada de llegada (RN-09)
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


            {cargando && <Cargando />}


            {!cargando && error && <div className="alert alert-danger">{error}</div>}


            {!cargando && !error && reservas.length === 0 && (
                <div className="text-center text-body-secondary py-5">
                    <i className="bi bi-inbox fs-1 d-block mb-2" aria-hidden="true"></i>
                    No hay reservas en estado {estado}.
                </div>
            )}


            {!cargando && !error && reservas.length > 0 && datos && (
                <>
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
                    <Paginacion paginaActual={datos.paginaActual} totalPaginas={datos.totalPaginas} alCambiar={cambiarPagina} />
                </>
            )}
        </section>
    );
}

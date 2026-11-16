import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react';
import { useAuth } from '../hooks/useAuth';
import { GestionPersonal } from '../componentes/GestionPersonal';
import type { Apartamento } from '../modelos/apartamento';
import {
    bloquearApartamento,
    listarApartamentos,
    listarBloqueos,
    type BloqueoApartamento,
} from '../servicios/administracionServicio';
import { mensajeDeError } from '../utilidades/errores';
import { notificarError, notificarExito } from '../utilidades/notificaciones';

export default function Administracion() {
    const { tieneRol } = useAuth();
    const esAdministrador = tieneRol('ADMINISTRADOR');
    const [apartamentos, setApartamentos] = useState<Apartamento[]>([]);
    const [seleccion, setSeleccion] = useState('');
    const [bloqueos, setBloqueos] = useState<BloqueoApartamento[]>([]);
    const [fechaInicio, setFechaInicio] = useState('');
    const [fechaFin, setFechaFin] = useState('');
    const [motivo, setMotivo] = useState('');
    const [cargando, setCargando] = useState(true);
    const [guardando, setGuardando] = useState(false);

    const apartamentoSeleccionado = useMemo(
        () => apartamentos.find((apartamento) => apartamento.identificacion === seleccion),
        [apartamentos, seleccion],
    );

    const cargarApartamentos = useCallback(async () => {
        try {
            setCargando(true);
            const resultado = await listarApartamentos();
            setApartamentos(resultado);
            setSeleccion((actual) => actual || resultado[0]?.identificacion || '');
        } catch (error) {
            notificarError(mensajeDeError(error));
        } finally {
            setCargando(false);
        }
    }, []);

    const cargarBloqueos = useCallback(async () => {
        if (!seleccion) {
            setBloqueos([]);
            return;
        }
        try {
            setBloqueos(await listarBloqueos(seleccion));
        } catch (error) {
            notificarError(mensajeDeError(error));
        }
    }, [seleccion]);

    useEffect(() => { void cargarApartamentos(); }, [cargarApartamentos]);
    useEffect(() => { void cargarBloqueos(); }, [cargarBloqueos]);

    async function enviarBloqueo(evento: FormEvent<HTMLFormElement>) {
        evento.preventDefault();
        if (!seleccion || !fechaInicio || !fechaFin || !motivo.trim()) return;
        if (fechaFin <= fechaInicio) {
            notificarError('La fecha final debe ser posterior a la fecha inicial.');
            return;
        }
        try {
            setGuardando(true);
            await bloquearApartamento(seleccion, { fechaInicio, fechaFin, motivo: motivo.trim() });
            notificarExito('Bloqueo registrado correctamente.');
            setFechaInicio('');
            setFechaFin('');
            setMotivo('');
            await cargarBloqueos();
        } catch (error) {
            notificarError(mensajeDeError(error));
        } finally {
            setGuardando(false);
        }
    }

    return (
        <section>
            <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-4">
                <div>
                    <h1 className="h3 mb-1">{esAdministrador ? 'Administración del alojamiento' : 'Gestión de apartamentos'}</h1>
                    <p className="text-body-secondary mb-0">
                        {esAdministrador
                            ? 'Administra el inventario y consulta los bloqueos operativos.'
                            : 'Consulta los apartamentos y registra bloqueos por mantenimiento u operación.'}
                    </p>
                </div>
                <span className="badge text-bg-secondary">{apartamentos.length} apartamentos</span>
            </div>

            {cargando ? <div className="text-center py-5"><div className="spinner-border" role="status"><span className="visually-hidden">Cargando…</span></div></div> : (
                <div className="row g-4">
                    <div className="col-12 col-lg-5">
                        <div className="card h-100">
                            <div className="card-header fw-semibold">Inventario de apartamentos</div>
                            <div className="list-group list-group-flush">
                                {apartamentos.map((apartamento) => (
                                    <button
                                        type="button"
                                        key={apartamento.identificacion}
                                        className={`list-group-item list-group-item-action text-start ${seleccion === apartamento.identificacion ? 'active' : ''}`}
                                        onClick={() => setSeleccion(apartamento.identificacion)}
                                    >
                                        <span className="d-flex justify-content-between align-items-center gap-2">
                                            <span>
                                                <strong>{apartamento.identificacion}</strong>
                                                <span className="d-block small">{apartamento.nombre}</span>
                                            </span>
                                            <span className={`badge ${apartamento.activo ? 'text-bg-success' : 'text-bg-secondary'}`}>
                                                {apartamento.activo ? 'Activo' : 'Retirado'}
                                            </span>
                                        </span>
                                        <small className="d-block mt-1">Capacidad: {apartamento.capacidad} · {apartamento.estadoOperativo.replaceAll('_', ' ')}</small>
                                    </button>
                                ))}
                                {apartamentos.length === 0 && <div className="p-3 text-body-secondary">No hay apartamentos registrados.</div>}
                            </div>
                        </div>
                    </div>

                    <div className="col-12 col-lg-7">
                        <div className="card mb-4">
                            <div className="card-header fw-semibold">Bloquear apartamento (RN-07)</div>
                            <div className="card-body">
                                {apartamentoSeleccionado ? <>
                                    <p className="text-body-secondary">Apartamento seleccionado: <strong>{apartamentoSeleccionado.identificacion} · {apartamentoSeleccionado.nombre}</strong></p>
                                    <form onSubmit={enviarBloqueo}>
                                        <div className="row g-3">
                                            <div className="col-sm-6">
                                                <label htmlFor="fechaInicioBloqueo" className="form-label">Fecha de inicio</label>
                                                <input id="fechaInicioBloqueo" className="form-control" type="date" value={fechaInicio} onChange={(e) => setFechaInicio(e.target.value)} required />
                                            </div>
                                            <div className="col-sm-6">
                                                <label htmlFor="fechaFinBloqueo" className="form-label">Fecha de fin</label>
                                                <input id="fechaFinBloqueo" className="form-control" type="date" min={fechaInicio || undefined} value={fechaFin} onChange={(e) => setFechaFin(e.target.value)} required />
                                            </div>
                                            <div className="col-12">
                                                <label htmlFor="motivoBloqueo" className="form-label">Motivo del bloqueo</label>
                                                <textarea id="motivoBloqueo" className="form-control" rows={2} maxLength={300} value={motivo} onChange={(e) => setMotivo(e.target.value)} placeholder="Ej. Mantenimiento preventivo" required />
                                            </div>
                                            <div className="col-12 d-flex justify-content-end">
                                                <button className="btn btn-primary" type="submit" disabled={guardando}>
                                                    {guardando ? 'Registrando…' : 'Registrar bloqueo'}
                                                </button>
                                            </div>
                                        </div>
                                    </form>
                                </> : <p className="text-body-secondary mb-0">Selecciona un apartamento para registrar un bloqueo.</p>}
                            </div>
                        </div>

                        <div className="card">
                            <div className="card-header fw-semibold">Historial de bloqueos</div>
                            <div className="table-responsive">
                                <table className="table table-hover align-middle mb-0">
                                    <thead><tr><th>Desde</th><th>Hasta</th><th>Motivo</th></tr></thead>
                                    <tbody>
                                        {bloqueos.map((bloqueo) => (
                                            <tr key={bloqueo.id}>
                                                <td>{bloqueo.fechaInicio}</td><td>{bloqueo.fechaFin}</td><td>{bloqueo.motivo}</td>
                                            </tr>
                                        ))}
                                        {bloqueos.length === 0 && <tr><td colSpan={3} className="text-center text-body-secondary py-4">No hay bloqueos registrados.</td></tr>}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {esAdministrador && <GestionPersonal />}
        </section>
    );
}

import { useCallback, useEffect, useState, type FormEvent } from 'react';
import {
    crearUsuarioInterno,
    listarUsuariosInternos,
    type RolInterno,
    type UsuarioInterno,
} from '../servicios/administracionServicio';
import { mensajeDeError } from '../utilidades/errores';
import { notificarError, notificarExito } from '../utilidades/notificaciones';

const fechaLocalHoy = () => {
    const hoy = new Date();
    hoy.setMinutes(hoy.getMinutes() - hoy.getTimezoneOffset());
    return hoy.toISOString().slice(0, 10);
};

const formularioInicial = {
    nombre: '',
    correo: '',
    contrasena: '',
    telefono: '',
    documento: '',
    fechaNacimiento: '',
    rol: 'RECEPCIONISTA' as RolInterno,
};

export function GestionPersonal() {
    const [usuarios, setUsuarios] = useState<UsuarioInterno[]>([]);
    const [formulario, setFormulario] = useState(formularioInicial);
    const [cargando, setCargando] = useState(true);
    const [guardando, setGuardando] = useState(false);

    const cargarUsuarios = useCallback(async () => {
        try {
            setCargando(true);
            setUsuarios(await listarUsuariosInternos());
        } catch (error) {
            notificarError(mensajeDeError(error));
        } finally {
            setCargando(false);
        }
    }, []);

    useEffect(() => { void cargarUsuarios(); }, [cargarUsuarios]);

    function actualizar<K extends keyof typeof formularioInicial>(campo: K, valor: (typeof formularioInicial)[K]) {
        setFormulario((actual) => ({ ...actual, [campo]: valor }));
    }

    async function enviarFormulario(evento: FormEvent<HTMLFormElement>) {
        evento.preventDefault();
        try {
            setGuardando(true);
            await crearUsuarioInterno({
                ...formulario,
                nombre: formulario.nombre.trim(),
                correo: formulario.correo.trim(),
                telefono: formulario.telefono.trim(),
                documento: formulario.documento.trim(),
            });
            notificarExito('La cuenta del personal fue creada correctamente.');
            setFormulario(formularioInicial);
            await cargarUsuarios();
        } catch (error) {
            notificarError(mensajeDeError(error));
        } finally {
            setGuardando(false);
        }
    }

    return (
        <section className="mt-5" aria-labelledby="tituloGestionPersonal">
            <div className="mb-3">
                <h2 id="tituloGestionPersonal" className="h4 mb-1">Gestión de personal</h2>
                <p className="text-body-secondary mb-0">
                    Crea y consulta las cuentas internas de recepción y personal de servicio.
                </p>
            </div>

            <div className="row g-4">
                <div className="col-12 col-lg-5">
                    <div className="card h-100">
                        <div className="card-header fw-semibold">Crear cuenta interna</div>
                        <div className="card-body">
                            <form onSubmit={enviarFormulario}>
                                <div className="mb-3">
                                    <label htmlFor="personalNombre" className="form-label">Nombre completo</label>
                                    <input id="personalNombre" className="form-control" maxLength={100} value={formulario.nombre}
                                        onChange={(e) => actualizar('nombre', e.target.value)} required />
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="personalCorreo" className="form-label">Correo electrónico</label>
                                    <input id="personalCorreo" className="form-control" type="email" maxLength={120} value={formulario.correo}
                                        onChange={(e) => actualizar('correo', e.target.value)} required />
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="personalContrasena" className="form-label">Contraseña inicial</label>
                                    <input id="personalContrasena" className="form-control" type="password" minLength={8} maxLength={72}
                                        pattern="(?=.*[A-Z])(?=.*[0-9]).{8,72}" title="Mínimo 8 caracteres, una mayúscula y un número."
                                        value={formulario.contrasena} onChange={(e) => actualizar('contrasena', e.target.value)} required />
                                    <div className="form-text">Mínimo 8 caracteres, con al menos una mayúscula y un número.</div>
                                </div>
                                <div className="row g-3 mb-3">
                                    <div className="col-sm-6">
                                        <label htmlFor="personalTelefono" className="form-label">Teléfono</label>
                                        <input id="personalTelefono" className="form-control" maxLength={20} value={formulario.telefono}
                                            onChange={(e) => actualizar('telefono', e.target.value)} required />
                                    </div>
                                    <div className="col-sm-6">
                                        <label htmlFor="personalDocumento" className="form-label">Documento</label>
                                        <input id="personalDocumento" className="form-control" minLength={7} maxLength={11} pattern="[A-Za-z0-9]+"
                                            value={formulario.documento} onChange={(e) => actualizar('documento', e.target.value)} required />
                                    </div>
                                </div>
                                <div className="mb-3">
                                    <label htmlFor="personalNacimiento" className="form-label">Fecha de nacimiento</label>
                                    <input id="personalNacimiento" className="form-control" type="date" max={fechaLocalHoy()}
                                        value={formulario.fechaNacimiento} onChange={(e) => actualizar('fechaNacimiento', e.target.value)} required />
                                </div>
                                <div className="mb-4">
                                    <label htmlFor="personalRol" className="form-label">Rol</label>
                                    <select id="personalRol" className="form-select" value={formulario.rol}
                                        onChange={(e) => actualizar('rol', e.target.value as RolInterno)}>
                                        <option value="RECEPCIONISTA">Recepcionista</option>
                                        <option value="PERSONAL_SERVICIO">Personal de servicio</option>
                                    </select>
                                </div>
                                <button className="btn btn-primary w-100" type="submit" disabled={guardando}>
                                    {guardando ? 'Creando cuenta…' : 'Crear cuenta'}
                                </button>
                            </form>
                        </div>
                    </div>
                </div>

                <div className="col-12 col-lg-7">
                    <div className="card">
                        <div className="card-header d-flex justify-content-between align-items-center gap-2">
                            <span className="fw-semibold">Cuentas internas registradas</span>
                            <button type="button" className="btn btn-sm btn-outline-secondary" onClick={() => void cargarUsuarios()} disabled={cargando}>
                                Actualizar
                            </button>
                        </div>
                        <div className="table-responsive">
                            <table className="table table-hover align-middle mb-0">
                                <thead><tr><th>Nombre</th><th>Correo</th><th>Rol</th><th>Estado</th></tr></thead>
                                <tbody>
                                    {usuarios.map((usuario) => (
                                        <tr key={usuario.id}>
                                            <td>
                                                <strong>{usuario.nombre}</strong>
                                                <small className="d-block text-body-secondary">CC: {usuario.documento}</small>
                                            </td>
                                            <td>{usuario.correo}</td>
                                            <td>{usuario.rol === 'RECEPCIONISTA' ? 'Recepcionista' : 'Personal de servicio'}</td>
                                            <td><span className={`badge ${usuario.activo ? 'text-bg-success' : 'text-bg-secondary'}`}>
                                                {usuario.activo ? 'Activo' : 'Inactivo'}
                                            </span></td>
                                        </tr>
                                    ))}
                                    {!cargando && usuarios.length === 0 && (
                                        <tr><td colSpan={4} className="text-center text-body-secondary py-4">Todavía no hay cuentas internas.</td></tr>
                                    )}
                                    {cargando && <tr><td colSpan={4} className="text-center py-4">Cargando cuentas…</td></tr>}
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div className="alert alert-info mt-3 mb-0" role="note">
                        La contraseña inicial no vuelve a mostrarse después de crear la cuenta. Compártela con el empleado por un canal seguro. El registro público continúa creando únicamente huéspedes.
                    </div>
                </div>
            </div>
        </section>
    );
}

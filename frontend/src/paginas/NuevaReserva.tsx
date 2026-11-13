import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useFieldArray, useForm, type Path } from 'react-hook-form';
import { esquemaNuevaReserva, type FormularioNuevaReserva } from '../esquemas/reserva';
import type { CrearReservaRequest } from '../modelos/reserva';
import { crearReserva } from '../servicios/reservaServicio';
import { erroresDeCampo, mensajeDeError } from '../utilidades/errores';


const OCUPANTE_VACIO = { documento: '', nombre: '', fechaNacimiento: '' };


// Clase condicional reutilizable: el campo se pinta en rojo si tiene error
function claseCampo(error: unknown) {
    return `form-control ${error ? 'is-invalid' : ''}`;
}


// Del formulario al DTO del backend. El primer ocupante es el titular, y la
// pantalla es de recepción: el canal de origen es DIRECTO (2.4).
function aSolicitud(datos: FormularioNuevaReserva): CrearReservaRequest {
    return {
        identificacionApartamento: datos.identificacionApartamento,
        fechaEntrada: datos.fechaEntrada,
        fechaSalida: datos.fechaSalida,
        horaEstimadaLlegada: datos.horaEstimadaLlegada || null,
        canalOrigen: 'DIRECTO',
        titular: datos.ocupantes[0],
        ocupantes: datos.ocupantes,
    };
}


export default function NuevaReserva() {
    const [codigoCreado, setCodigoCreado] = useState<string | null>(null);
    const {
        register,
        control,
        handleSubmit,
        reset,
        setError,
        formState: { errors, isSubmitting },
    } = useForm<FormularioNuevaReserva>({
        resolver: zodResolver(esquemaNuevaReserva),
        mode: 'onTouched',
        defaultValues: {
            identificacionApartamento: '',
            fechaEntrada: '',
            fechaSalida: '',
            horaEstimadaLlegada: '',
            ocupantes: [OCUPANTE_VACIO],
        },
    });


    // useFieldArray administra la lista de ocupantes: agregar, quitar y registrar cada campo
    const { fields, append, remove } = useFieldArray({ control, name: 'ocupantes' });


    async function alEnviar(datos: FormularioNuevaReserva) {
        setCodigoCreado(null);
        try {
            const reserva = await crearReserva(aSolicitud(datos));
            setCodigoCreado(reserva.codigo);
            reset();
        } catch (error) {
            // 400: el backend dice qué campo falló; se muestra junto al campo
            for (const [campo, mensaje] of Object.entries(erroresDeCampo(error))) {
                const ruta = campo.replace(/^titular\./, 'ocupantes.0.');
                setError(ruta as Path<FormularioNuevaReserva>, { message: mensaje });
            }
            // 404, 409 y cualquier otro: mensaje general con significado de negocio
            setError('root.servidor', { message: mensajeDeError(error) });
        }
    }


    return (
        <section>
            <h1 className="h3 mb-3">Nueva reserva <small className="text-body-secondary">(recepción)</small></h1>


            <form onSubmit={handleSubmit(alEnviar)} noValidate>
                <div className="row g-3">
                    <div className="col-12 col-md-6">
                        <label htmlFor="identificacionApartamento" className="form-label">Apartamento</label>
                        <input
                            id="identificacionApartamento"
                            placeholder="APT-301"
                            className={claseCampo(errors.identificacionApartamento)}
                            {...register('identificacionApartamento')}
                        />
                        <div className="invalid-feedback">{errors.identificacionApartamento?.message}</div>
                    </div>


                    <div className="col-12 col-md-6">
                        <label htmlFor="horaEstimadaLlegada" className="form-label">Hora estimada de llegada (opcional)</label>
                        <input
                            id="horaEstimadaLlegada"
                            type="time"
                            className={claseCampo(errors.horaEstimadaLlegada)}
                            {...register('horaEstimadaLlegada')}
                        />
                        <div className="invalid-feedback">{errors.horaEstimadaLlegada?.message}</div>
                    </div>


                    <div className="col-12 col-md-6">
                        <label htmlFor="fechaEntrada" className="form-label">Fecha de entrada</label>
                        <input id="fechaEntrada" type="date" className={claseCampo(errors.fechaEntrada)} {...register('fechaEntrada')} />
                        <div className="invalid-feedback">{errors.fechaEntrada?.message}</div>
                    </div>


                    <div className="col-12 col-md-6">
                        <label htmlFor="fechaSalida" className="form-label">Fecha de salida</label>
                        <input id="fechaSalida" type="date" className={claseCampo(errors.fechaSalida)} {...register('fechaSalida')} />
                        <div className="invalid-feedback">{errors.fechaSalida?.message}</div>
                    </div>
                </div>


                <fieldset className="mt-4">
                    <legend className="h5">Ocupantes <small className="text-body-secondary">(el primero es el titular)</small></legend>


                    {fields.map((campo, indice) => (
                        <div key={campo.id} className="card mb-3">
                            <div className="card-header d-flex justify-content-between align-items-center">
                                <strong>{indice === 0 ? 'Titular' : `Ocupante ${indice + 1}`}</strong>
                                {indice > 0 && (
                                    <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => remove(indice)}>
                                        Quitar
                                    </button>
                                )}
                            </div>
                            <div className="card-body row g-3">
                                <div className="col-12 col-md-4">
                                    <label htmlFor={`documento-${indice}`} className="form-label">Documento</label>
                                    <input
                                        id={`documento-${indice}`}
                                        className={claseCampo(errors.ocupantes?.[indice]?.documento)}
                                        {...register(`ocupantes.${indice}.documento`)}
                                    />
                                    <div className="invalid-feedback">{errors.ocupantes?.[indice]?.documento?.message}</div>
                                </div>
                                <div className="col-12 col-md-4">
                                    <label htmlFor={`nombre-${indice}`} className="form-label">Nombre</label>
                                    <input
                                        id={`nombre-${indice}`}
                                        className={claseCampo(errors.ocupantes?.[indice]?.nombre)}
                                        {...register(`ocupantes.${indice}.nombre`)}
                                    />
                                    <div className="invalid-feedback">{errors.ocupantes?.[indice]?.nombre?.message}</div>
                                </div>
                                <div className="col-12 col-md-4">
                                    <label htmlFor={`fechaNacimiento-${indice}`} className="form-label">Fecha de nacimiento</label>
                                    <input
                                        id={`fechaNacimiento-${indice}`}
                                        type="date"
                                        className={claseCampo(errors.ocupantes?.[indice]?.fechaNacimiento)}
                                        {...register(`ocupantes.${indice}.fechaNacimiento`)}
                                    />
                                    <div className="invalid-feedback">{errors.ocupantes?.[indice]?.fechaNacimiento?.message}</div>
                                </div>
                            </div>
                        </div>
                    ))}


                    <button type="button" className="btn btn-outline-secondary" onClick={() => append(OCUPANTE_VACIO)}>
                        <i className="bi bi-person-plus me-1" aria-hidden="true"></i>Agregar ocupante
                    </button>
                </fieldset>


                <div className="mt-4">
                    <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                        {isSubmitting ? 'Creando…' : 'Crear reserva'}
                    </button>
                </div>


                {errors.root?.servidor && <div className="alert alert-danger mt-3">{errors.root.servidor.message}</div>}
                {codigoCreado && <div className="alert alert-success mt-3">Reserva {codigoCreado} creada en estado PENDIENTE.</div>}
            </form>
        </section>
    );
}

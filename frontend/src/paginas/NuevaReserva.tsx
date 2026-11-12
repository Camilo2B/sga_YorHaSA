// src/paginas/NuevaReserva.tsx
import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useFieldArray, useForm, type Path } from 'react-hook-form';
import { useSearchParams } from 'react-router';
import { esquemaNuevaReserva, type FormularioNuevaReserva } from '../esquemas/reserva';
import type { CrearReservaRequest } from '../modelos/reserva';
import { crearReserva } from '../servicios/reservaServicio';
import { erroresDeCampo, mensajeDeError } from '../utilidades/errores';

const OCUPANTE_VACIO = { documento: '', nombre: '', fechaNacimiento: '' };

// Del formulario al DTO del backend. El titular es el primer ocupante,
// y los ocupantes adicionales van a partir del segundo (slice(1)).
function aSolicitud(datos: FormularioNuevaReserva): CrearReservaRequest {
    return {
        identificacionApartamento: datos.identificacionApartamento,
        fechaEntrada: datos.fechaEntrada,
        fechaSalida: datos.fechaSalida,
        horaEstimadaLlegada: datos.horaEstimadaLlegada || null,
        canalOrigen: 'DIRECTO',
        titular: datos.ocupantes[0],
        ocupantes: datos.ocupantes.slice(1),
    };
}

// El backend numera los acompañantes desde 0 (ocupantes[0] es el primer acompañante) y el titular va aparte;
// en el formulario el titular es el ocupante 0, así que los acompañantes se corren un lugar.
function aRutaDelFormulario(campo: string): string {
    if (campo.startsWith('titular.')) {
        return campo.replace(/^titular\./, 'ocupantes.0.');
    }
    return campo.replace(/^ocupantes\.(\d+)/, (_, indice: string) => `ocupantes.${Number(indice) + 1}`);
}

export default function NuevaReserva() {
    // /reservas/nueva?apartamento=ATK-001 deja el apartamento ya escrito
    const [parametros] = useSearchParams();
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
            identificacionApartamento: parametros.get('apartamento') ?? '',
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
            // 400: el backend dice qué campo falló; se muestra junto al campo correspondiente
            const camposError = erroresDeCampo(error);
            if (Object.keys(camposError).length > 0) {
                for (const [campo, mensaje] of Object.entries(camposError)) {
                    setError(aRutaDelFormulario(campo) as Path<FormularioNuevaReserva>, { message: mensaje });
                }
            } else {
                // Si el 400 no especifica campos concretos, mostramos el mensaje general del servidor
                setError('root.servidor', { message: mensajeDeError(error) });
            }
        }
    }

    return (
        <section>
            <h1>Nueva reserva (recepción)</h1>

            <form onSubmit={handleSubmit(alEnviar)} noValidate>
                <div>
                    <label htmlFor="identificacionApartamento">Apartamento</label>
                    <input id="identificacionApartamento" placeholder="APT-301" {...register('identificacionApartamento')} />
                    {errors.identificacionApartamento && <p className="error">{errors.identificacionApartamento.message}</p>}
                </div>

                <div>
                    <label htmlFor="fechaEntrada">Fecha de entrada</label>
                    <input id="fechaEntrada" type="date" {...register('fechaEntrada')} />
                    {errors.fechaEntrada && <p className="error">{errors.fechaEntrada.message}</p>}
                </div>

                <div>
                    <label htmlFor="fechaSalida">Fecha de salida</label>
                    <input id="fechaSalida" type="date" {...register('fechaSalida')} />
                    {errors.fechaSalida && <p className="error">{errors.fechaSalida.message}</p>}
                </div>

                <div>
                    <label htmlFor="horaEstimadaLlegada">Hora estimada de llegada (opcional)</label>
                    <input id="horaEstimadaLlegada" type="time" {...register('horaEstimadaLlegada')} />
                    {errors.horaEstimadaLlegada && <p className="error">{errors.horaEstimadaLlegada.message}</p>}
                </div>

                <fieldset>
                    <legend>Ocupantes (el primero es el titular)</legend>

                    {fields.map((campo, indice) => (
                        <div key={campo.id} className="tarjeta">
                            <strong>{indice === 0 ? 'Titular' : `Ocupante ${indice + 1}`}</strong>

                            <div>
                                <label htmlFor={`documento-${indice}`}>Documento</label>
                                <input id={`documento-${indice}`} {...register(`ocupantes.${indice}.documento`)} />
                                {errors.ocupantes?.[indice]?.documento && (
                                    <p className="error">{errors.ocupantes[indice].documento.message}</p>
                                )}
                            </div>

                            <div>
                                <label htmlFor={`nombre-${indice}`}>Nombre</label>
                                <input id={`nombre-${indice}`} {...register(`ocupantes.${indice}.nombre`)} />
                                {errors.ocupantes?.[indice]?.nombre && (
                                    <p className="error">{errors.ocupantes[indice].nombre.message}</p>
                                )}
                            </div>

                            <div>
                                <label htmlFor={`fechaNacimiento-${indice}`}>Fecha de nacimiento</label>
                                <input id={`fechaNacimiento-${indice}`} type="date" {...register(`ocupantes.${indice}.fechaNacimiento`)} />
                                {errors.ocupantes?.[indice]?.fechaNacimiento && (
                                    <p className="error">{errors.ocupantes[indice].fechaNacimiento.message}</p>
                                )}
                            </div>

                            {indice > 0 && (
                                <button type="button" onClick={() => remove(indice)}>Quitar</button>
                            )}
                        </div>
                    ))}

                    <button type="button" onClick={() => append(OCUPANTE_VACIO)}>Agregar ocupante</button>
                </fieldset>

                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Creando…' : 'Crear reserva'}
                </button>

                {errors.root?.servidor && <p className="error">{errors.root.servidor.message}</p>}
                {codigoCreado && <p className="exito">Reserva {codigoCreado} creada en estado PENDIENTE.</p>}
            </form>
        </section>
    );
}
// src/paginas/Cotizacion.tsx
import { zodResolver } from '@hookform/resolvers/zod';
import { useFieldArray, useForm } from 'react-hook-form';
import { useState } from 'react';
import { esquemaCotizacion, type FormularioCotizacion } from '../esquemas/cotizacion';
import { cotizarEstancia } from '../servicios/cotizacionServicio';
import type { CotizacionResponse } from '../modelos/cotizacion';
import { mensajeDeError } from '../utilidades/errores';

export default function Cotizacion() {
    const [resultado, setResultado] = useState<CotizacionResponse | null>(null);

    const {
        register,
        control,
        handleSubmit,
        setError,
        formState: { errors, isSubmitting },
    } = useForm<FormularioCotizacion>({
        resolver: zodResolver(esquemaCotizacion),
        defaultValues: {
            identificacionApartamento: '',
            fechaEntrada: '',
            fechaSalida: '',
            fechasNacimientoOcupantes: [{ fecha: '' }],
        },
    });

    const { fields, append, remove } = useFieldArray({
        control,
        name: 'fechasNacimientoOcupantes',
    });

    async function alEnviar(datos: FormularioCotizacion) {
        try {
            setResultado(null);
            // Transformamos el array de objetos a un array plano de strings que espera el backend
            const payload = {
                identificacionApartamento: datos.identificacionApartamento,
                fechaEntrada: datos.fechaEntrada,
                fechaSalida: datos.fechaSalida,
                fechasNacimientoOcupantes: datos.fechasNacimientoOcupantes.map((o) => o.fecha),
            };

            const cotizacion = await cotizarEstancia(payload);
            setResultado(cotizacion);
        } catch (error) {
            setError('root.servidor', {
                message: mensajeDeError(error),
            });
        }
    }

    return (
        <section>
            <h1>Cotizar Estancia</h1>

            <form onSubmit={handleSubmit(alEnviar)} noValidate>
                <div>
                    <label htmlFor="identificacionApartamento">Identificación del Apartamento</label>
                    <input id="identificacionApartamento" type="text" {...register('identificacionApartamento')} />
                    {errors.identificacionApartamento && <p className="error">{errors.identificacionApartamento.message}</p>}
                </div>

                <div>
                    <label htmlFor="fechaEntrada">Fecha de Entrada</label>
                    <input id="fechaEntrada" type="date" {...register('fechaEntrada')} />
                    {errors.fechaEntrada && <p className="error">{errors.fechaEntrada.message}</p>}
                </div>

                <div>
                    <label htmlFor="fechaSalida">Fecha de Salida</label>
                    <input id="fechaSalida" type="date" {...register('fechaSalida')} />
                    {errors.fechaSalida && <p className="error">{errors.fechaSalida.message}</p>}
                </div>

                <fieldset>
                    <legend>Ocupantes</legend>
                    {fields.map((field, index) => (
                        <div key={field.id} style={{ display: 'flex', gap: '1rem', marginBottom: '0.5rem' }}>
                            <div>
                                <label>Fecha de Nacimiento Ocupante {index + 1}</label>
                                <input type="date" {...register(`fechasNacimientoOcupantes.${index}.fecha` as const)} />
                                {errors.fechasNacimientoOcupantes?.[index]?.fecha && (
                                    <p className="error">{errors.fechasNacimientoOcupantes[index]?.fecha?.message}</p>
                                )}
                            </div>
                            {fields.length > 1 && (
                                <button type="button" onClick={() => remove(index)}>
                                    Eliminar
                                </button>
                            )}
                        </div>
                    ))}
                    <button type="button" onClick={() => append({ fecha: '' })}>
                        + Añadir Ocupante
                    </button>
                    {errors.fechasNacimientoOcupantes && !Array.isArray(errors.fechasNacimientoOcupantes) && (
                        <p className="error">{errors.fechasNacimientoOcupantes.message}</p>
                    )}
                </fieldset>

                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Calculando cotización…' : 'Cotizar'}
                </button>

                {errors.root?.servidor && <p className="error">{errors.root.servidor.message}</p>}
            </form>

            {resultado && (
                <div style={{ marginTop: '2rem', padding: '1rem', border: '1px solid #ccc' }}>
                    <h2>Resultado de la Cotización</h2>
                    <p><strong>Noches:</strong> {resultado.noches}</p>
                    <p><strong>Valor Total:</strong> {resultado.valorTotal.toLocaleString()} {resultado.moneda}</p>
                </div>
            )}
        </section>
    );
}
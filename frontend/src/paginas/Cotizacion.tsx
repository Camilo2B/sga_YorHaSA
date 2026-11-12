// src/paginas/Cotizacion.tsx
import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useFieldArray, useForm, type Path } from 'react-hook-form';
import { useSearchParams } from 'react-router';
import { esquemaCotizacion, type FormularioCotizacion } from '../esquemas/cotizacion';
import type { CotizacionResponse } from '../modelos/cotizacion';
import { cotizarEstancia } from '../servicios/cotizacionServicio';
import { erroresDeCampo, mensajeDeError } from '../utilidades/errores';

const OCUPANTE_VACIO = { documento: '', nombre: '', fechaNacimiento: '' };

export default function Cotizacion() {
    // /cotizaciones?apartamento=ATK-001 deja el apartamento ya escrito
    const [parametros] = useSearchParams();
    const [resultado, setResultado] = useState<CotizacionResponse | null>(null);

    const {
        register,
        control,
        handleSubmit,
        setError,
        formState: { errors, isSubmitting },
    } = useForm<FormularioCotizacion>({
        resolver: zodResolver(esquemaCotizacion),
        mode: 'onTouched',
        defaultValues: {
            identificacionApartamento: parametros.get('apartamento') ?? '',
            fechaEntrada: '',
            fechaSalida: '',
            ocupantes: [OCUPANTE_VACIO],
        },
    });

    const { fields, append, remove } = useFieldArray({ control, name: 'ocupantes' });

    async function alEnviar(datos: FormularioCotizacion) {
        setResultado(null);
        try {
            // El formulario y el DTO CotizarEstanciaRequest tienen la misma forma: se envía tal cual
            setResultado(await cotizarEstancia(datos));
        } catch (error) {
            // 400: el backend dice qué campo falló y se muestra junto a ese campo
            const deCampo = Object.entries(erroresDeCampo(error));
            if (deCampo.length > 0) {
                for (const [campo, mensaje] of deCampo) {
                    setError(campo as Path<FormularioCotizacion>, { message: mensaje });
                }
            } else {
                // 409 (regla de negocio) u otro error: mensaje general del servidor
                setError('root.servidor', { message: mensajeDeError(error) });
            }
        }
    }

    return (
        <section>
            <h1>Cotizar estancia</h1>

            <form onSubmit={handleSubmit(alEnviar)} noValidate>
                <div>
                    <label htmlFor="identificacionApartamento">Apartamento</label>
                    <input id="identificacionApartamento" placeholder="ATK-001" {...register('identificacionApartamento')} />
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

                <fieldset>
                    <legend>Ocupantes</legend>

                    {fields.map((campo, indice) => (
                        <div key={campo.id} className="tarjeta">
                            <strong>Ocupante {indice + 1}</strong>

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
                    {errors.ocupantes?.message && <p className="error">{errors.ocupantes.message}</p>}
                </fieldset>

                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Calculando cotización…' : 'Cotizar'}
                </button>

                {errors.root?.servidor && <p className="error">{errors.root.servidor.message}</p>}
            </form>

            {resultado && (
                <div className="tarjeta" style={{ marginTop: '2rem' }}>
                    <h2>Resultado de la cotización</h2>
                    <p><strong>Noches:</strong> {resultado.noches}</p>
                    <p><strong>Ocupantes facturables:</strong> {resultado.ocupantesFacturables}</p>
                    <p><strong>Valor total:</strong> {resultado.valorTotal.toLocaleString('es-CO')} {resultado.moneda}</p>
                </div>
            )}
        </section>
    );
}

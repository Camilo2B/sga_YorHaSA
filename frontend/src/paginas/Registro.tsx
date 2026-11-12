import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router';
import { esquemaRegistro, type FormularioRegistro } from '../esquemas/registro';
import { registrar } from '../servicios/authServicio';
import { erroresDeCampo, mensajeDeError } from '../utilidades/errores';

// Campos del formulario que el backend puede señalar en un 400 (nombres idénticos al DTO)
const CAMPOS = ['nombre', 'correo', 'documento', 'telefono', 'fechaNacimiento', 'contrasena'] as const;

export default function Registro() {
    const navigate = useNavigate();
    const {
        register,
        handleSubmit,
        setError,
        formState: { errors, isSubmitting },
    } = useForm<FormularioRegistro>({
        resolver: zodResolver(esquemaRegistro),
        mode: 'onTouched',
        defaultValues: {
            nombre: '',
            correo: '',
            documento: '',
            telefono: '',
            fechaNacimiento: '',
            contrasena: '',
            confirmarContrasena: '',
        },
    });

    async function alEnviar(datos: FormularioRegistro) {
        try {
            // Excluimos 'confirmarContrasena' antes de enviar los datos al DTO del backend
            const { confirmarContrasena, ...datosBackend } = datos;
            await registrar(datosBackend);
            navigate('/login'); // Redirige al login tras un registro exitoso
        } catch (error) {
            // 400: el backend dice qué campo falló y se muestra junto a ese campo
            const deCampo = Object.entries(erroresDeCampo(error)).filter(([campo]) =>
                (CAMPOS as readonly string[]).includes(campo),
            );
            if (deCampo.length > 0) {
                for (const [campo, mensaje] of deCampo) {
                    setError(campo as (typeof CAMPOS)[number], { message: mensaje });
                }
            } else {
                // 409 (correo o documento ya registrados) u otro error: mensaje general del servidor
                setError('root.servidor', { message: mensajeDeError(error) });
            }
        }
    }

    return (
        <section>
            <h1>Registro de Huésped</h1>

            <form onSubmit={handleSubmit(alEnviar)} noValidate>
                <div>
                    <label htmlFor="nombre">Nombre completo</label>
                    <input id="nombre" type="text" autoComplete="name" {...register('nombre')} />
                    {errors.nombre && <p className="error">{errors.nombre.message}</p>}
                </div>

                <div>
                    <label htmlFor="correo">Correo electrónico</label>
                    <input id="correo" type="email" autoComplete="email" {...register('correo')} />
                    {errors.correo && <p className="error">{errors.correo.message}</p>}
                </div>

                <div>
                    <label htmlFor="documento">Documento de identidad</label>
                    <input id="documento" type="text" {...register('documento')} />
                    {errors.documento && <p className="error">{errors.documento.message}</p>}
                </div>

                <div>
                    <label htmlFor="telefono">Teléfono</label>
                    <input id="telefono" type="tel" autoComplete="tel" {...register('telefono')} />
                    {errors.telefono && <p className="error">{errors.telefono.message}</p>}
                </div>

                <div>
                    <label htmlFor="fechaNacimiento">Fecha de nacimiento</label>
                    <input id="fechaNacimiento" type="date" {...register('fechaNacimiento')} />
                    {errors.fechaNacimiento && <p className="error">{errors.fechaNacimiento.message}</p>}
                </div>

                <div>
                    <label htmlFor="contrasena">Contraseña</label>
                    <input id="contrasena" type="password" autoComplete="new-password" {...register('contrasena')} />
                    {errors.contrasena && <p className="error">{errors.contrasena.message}</p>}
                </div>

                <div>
                    <label htmlFor="confirmarContrasena">Confirmar contraseña</label>
                    <input id="confirmarContrasena" type="password" autoComplete="new-password" {...register('confirmarContrasena')} />
                    {errors.confirmarContrasena && <p className="error">{errors.confirmarContrasena.message}</p>}
                </div>

                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Registrando…' : 'Registrarse'}
                </button>

                {errors.root?.servidor && <p className="error">{errors.root.servidor.message}</p>}
            </form>
        </section>
    );
}
// src/paginas/Login.tsx
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router';
import { esquemaLogin, type FormularioLogin } from '../esquemas/login';
import { login } from '../servicios/authServicio';
import { guardarSesion } from '../servicios/sesion';
import { estadoHttp, mensajeDeError } from '../utilidades/errores';

export default function Login() {
    const navigate = useNavigate();
    const {
        register,
        handleSubmit,
        setError,
        formState: { errors, isSubmitting },
    } = useForm<FormularioLogin>({
        resolver: zodResolver(esquemaLogin),
        mode: 'onTouched',
        defaultValues: { correo: '', contrasena: '' },
    });

    async function alEnviar(datos: FormularioLogin) {
        try {
            const sesion = await login(datos);
            guardarSesion(sesion);
            navigate('/');
        } catch (error) {
            setError('root.servidor', {
                message: estadoHttp(error) === 401
                    ? 'Correo o contraseña incorrectos'
                    : mensajeDeError(error),
            });
        }
    }

    return (
        <section>
            <h1>Ingresar</h1>

            <form onSubmit={handleSubmit(alEnviar)} noValidate>
                <div>
                    <label htmlFor="correo">Correo electrónico</label>
                    <input id="correo" type="email" autoComplete="email" {...register('correo')} />
                    {errors.correo && <p className="error">{errors.correo.message}</p>}
                </div>

                <div>
                    <label htmlFor="contrasena">Contraseña</label>
                    <input id="contrasena" type="password" autoComplete="current-password" {...register('contrasena')} />
                    {errors.contrasena && <p className="error">{errors.contrasena.message}</p>}
                </div>

                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Ingresando…' : 'Ingresar'}
                </button>

                {errors.root?.servidor && <p className="error">{errors.root.servidor.message}</p>}
            </form>
        </section>
    );
}
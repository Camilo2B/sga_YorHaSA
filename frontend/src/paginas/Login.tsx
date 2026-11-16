import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { Campo } from '../componentes/Campo';
import { esquemaLogin, type FormularioLogin } from '../esquemas/login';
import { useAuth } from '../hooks/useAuth';
import { estadoHttp, mensajeDeError } from '../utilidades/errores';


export default function Login() {
    const { iniciarSesion } = useAuth();
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
            // Al cambiar la sesión, RutaPublica redirige: aquí no se navega
            await iniciarSesion(datos);
        } catch (error) {
            setError('root.servidor', {
                message: estadoHttp(error) === 401
                    ? 'Correo o contraseña incorrectos'
                    : mensajeDeError(error),
            });
        }
    }


    return (
        <div className="row justify-content-center">
            <div className="col-12 col-sm-10 col-md-7 col-lg-5">
                <div className="card shadow-sm">
                    <div className="card-header text-center py-3">
                        <h1 className="h5 mb-0">
                            <i className="bi bi-box-arrow-in-right me-2" aria-hidden="true"></i>Ingresar
                        </h1>
                    </div>
                    <div className="card-body">
                        <form onSubmit={handleSubmit(alEnviar)} noValidate>
                            <Campo
                                id="username"
                                etiqueta="Correo electrónico"
                                type="email"
                                autoComplete="username"
                                placeholder="correo@ejemplo.com"
                                error={errors.correo?.message}
                                {...register('correo')}
                            />


                            <Campo
                                id="password"
                                etiqueta="Contraseña"
                                type="password"
                                autoComplete="current-password"
                                error={errors.contrasena?.message}
                                {...register('contrasena')}
                            />


                            <div className="d-grid">
                                <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                                    {isSubmitting && (
                                        <span className="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
                                    )}
                                    {isSubmitting ? 'Ingresando…' : 'Ingresar'}
                                </button>
                            </div>


                            {errors.root?.servidor && (
                                <div className="alert alert-danger mt-3 mb-0">{errors.root.servidor.message}</div>
                            )}
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
}

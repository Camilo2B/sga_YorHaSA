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
                            <div className="mb-3">
                                <label htmlFor="username" className="form-label">Correo electrónico</label>
                                <input
                                    id="username"
                                    type="email"
                                    autoComplete="username"
                                    placeholder="correo@ejemplo.com"
                                    className={`form-control ${errors.correo ? 'is-invalid' : ''}`}
                                    {...register('correo')}
                                />
                                {/* Bootstrap solo muestra .invalid-feedback si el campo anterior tiene .is-invalid */}
                                <div className="invalid-feedback">{errors.correo?.message}</div>
                            </div>


                            <div className="mb-3">
                                <label htmlFor="password" className="form-label">Contraseña</label>
                                <input
                                    id="password"
                                    type="password"
                                    autoComplete="current-password"
                                    className={`form-control ${errors.contrasena ? 'is-invalid' : ''}`}
                                    {...register('contrasena')}
                                />
                                <div className="invalid-feedback">{errors.contrasena?.message}</div>
                            </div>


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

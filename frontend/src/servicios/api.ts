import axios from 'axios';
import { borrarSesion, obtenerToken } from './sesion';

// Cliente HTTP único de la aplicación: todos los servicios lo usan.
// La URL del backend se puede cambiar con VITE_API_URL (archivo .env) sin tocar el código.
export const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
    timeout: 10_000,   // milisegundos: pasado este tiempo, la petición falla
});

// Las rutas /auth/* son públicas: allí no se envía token (un token vencido haría fallar hasta el login).
function esRutaPublica(url: string | undefined): boolean {
    return url?.startsWith('/auth/') ?? false;
}

// Interceptor de petición: todas las peticiones protegidas llevan el JWT sin que cada servicio lo arme.
api.interceptors.request.use((config) => {
    const token = obtenerToken();
    if (token && !esRutaPublica(config.url)) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Interceptor de respuesta: un 401 en una ruta protegida significa que no hay sesión o ya venció.
// Se borra la sesión y se lleva al usuario al login.
api.interceptors.response.use(
    (respuesta) => respuesta,
    (error: unknown) => {
        if (
            axios.isAxiosError(error) &&
            error.response?.status === 401 &&
            !esRutaPublica(error.config?.url)
        ) {
            borrarSesion();
            if (window.location.pathname !== '/login') {
                window.location.assign('/login?requerido=1');
            }
        }
        return Promise.reject(error);
    },
);

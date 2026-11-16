import axios from 'axios';
import { obtenerToken } from './sesion';

// Cliente HTTP único de la aplicación: todos los servicios lo usan.
// La URL ya no está quemada: Vite la toma de .env.development o .env.production.
export const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL?.trim() || '/api',
    timeout: 70_000,
});

// ---------- Interceptor de PETICIÓN: agrega el token a todas ----------
api.interceptors.request.use((config) => {
    const token = obtenerToken();
    // El login no lleva token: si llevara uno vencido, Spring Security
    // rechazaría la petición con 401 aunque /api/auth/login sea pública.
    const esAutenticacion = config.url?.startsWith('/auth/');
    if (token && !esAutenticacion) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// ---------- Interceptor de RESPUESTA: maneja el 401 en un solo lugar ----------
// Axios no vive dentro de React: no puede usar hooks ni cambiar el estado de un
// componente. El AuthProvider le deja aquí una función para avisarle del 401.
type ManejadorNoAutorizado = () => void;
let manejadorNoAutorizado: ManejadorNoAutorizado | null = null;

export function registrarManejadorNoAutorizado(manejador: ManejadorNoAutorizado) {
    manejadorNoAutorizado = manejador;
    return () => {
        if (manejadorNoAutorizado === manejador) manejadorNoAutorizado = null;
    };
}

api.interceptors.response.use(
    (respuesta) => respuesta,   // 2xx: pasa sin cambios
    (error: unknown) => {
        if (
            axios.isAxiosError(error) &&
            error.response?.status === 401 &&
            !error.config?.url?.startsWith('/auth/')   // un 401 del login son credenciales malas, no sesión vencida
        ) {
            manejadorNoAutorizado?.();
        }
        return Promise.reject(error);   // el componente sigue recibiendo el error
    },
);

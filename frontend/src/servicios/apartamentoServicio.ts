import { api } from './api';

/**
 * POST /api/apartamentos/{identificacion}/imagenes  (multipart/form-data)
 * Cada archivo viaja como una parte llamada "imagenes"; "principal" indica
 * cuál de ellas es la imagen destacada (7.3).
 */
export async function subirImagenes(
    identificacion: string,
    archivos: File[],
    principal: number,
    alProgresar?: (porcentaje: number) => void,
): Promise<void> {
    const datos = new FormData();
    for (const archivo of archivos) {
        datos.append('imagenes', archivo);   // mismo nombre repetido = lista en el backend
    }
    datos.append('principal', String(principal));

    // No se pone Content-Type: el navegador lo arma con el "boundary" que separa las partes
    await api.post(`/apartamentos/${identificacion}/imagenes`, datos, {
        timeout: 60_000,   // subir archivos tarda más que una consulta
        onUploadProgress: (evento) => {
            if (evento.total) {
                alProgresar?.(Math.round((evento.loaded * 100) / evento.total));
            }
        },
    });
}

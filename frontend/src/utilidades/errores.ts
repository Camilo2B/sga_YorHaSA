import axios from 'axios';
import type { ErrorResponse } from '../modelos/error';

/**
 * Traduce cualquier error de una petición a un mensaje para el usuario.
 * Si el backend envió un ErrorResponse (Guía 08), se usa su mensaje de negocio.
 */
export function mensajeDeError(error: unknown): string {
    if (!axios.isAxiosError<ErrorResponse>(error)) {
        return 'Ocurrió un error inesperado.';
    }
    if (!error.response) {
        // Sin respuesta: el backend está apagado, la URL está mal o CORS bloqueó la petición
        return 'No fue posible conectar con el servidor.';
    }
    const { status, data } = error.response;
    if (data?.mensaje) {
        // En un 400 de validación el mensaje es genérico: el detalle dice qué campo falló
        return data.detalles?.length ? `${data.mensaje}: ${data.detalles.join('; ')}` : data.mensaje;
    }
    if (status === 403) {
        return 'No tiene permisos para realizar esta acción.';
    }
    return `Error inesperado del servidor (${status}).`;
}

/** Código HTTP de la respuesta, o undefined si no hubo respuesta. */
export function estadoHttp(error: unknown): number | undefined {
    return axios.isAxiosError(error) ? error.response?.status : undefined;
}

/**
 * Errores de campo de un 400 de Bean Validation. El backend envía una línea "campo: mensaje" por cada
 * campo inválido; aquí se convierten al nombre que usa React Hook Form: "ocupantes[1].documento" →
 * "ocupantes.1.documento". Las líneas sin campo (errores globales) se ignoran: quedan en mensajeDeError.
 */
export function erroresDeCampo(error: unknown): Record<string, string> {
    if (!axios.isAxiosError<ErrorResponse>(error)) {
        return {};
    }
    const resultado: Record<string, string> = {};
    for (const linea of error.response?.data?.detalles ?? []) {
        const separador = linea.indexOf(': ');
        if (separador === -1) {
            continue;
        }
        const campo = linea.slice(0, separador).replace(/\[(\d+)\]/g, '.$1');
        resultado[campo] = linea.slice(separador + 2);
    }
    return resultado;
}

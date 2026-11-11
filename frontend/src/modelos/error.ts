// src/modelos/error.ts

/**
 * Espejo del ErrorResponse del backend (Guía 08 y 12):
 * representa la estructura estándar de todas las respuestas de error de la API.
 */
export interface ErrorResponse {
    timestamp: string;
    status: number;
    error: string;
    mensaje: string;                         // Mensaje con significado de negocio
    path: string;
    errores: Record<string, string> | null;  // Solo presente en un 400 de validación: mapea campo -> mensaje
}
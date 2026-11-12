// src/modelos/error.ts

/**
 * Espejo del ErrorResponse del backend (record ErrorResponse en infrastructure.rest.exception):
 * es el único formato de error de la API.
 */
export interface ErrorResponse {
    codigoError: string;   // Código estable: VALIDACION, REGLA_DOMINIO, NO_ENCONTRADO, ...
    mensaje: string;       // Mensaje con significado de negocio
    detalles: string[];    // Una línea "campo: mensaje" por campo inválido (solo en un 400 de validación)
}

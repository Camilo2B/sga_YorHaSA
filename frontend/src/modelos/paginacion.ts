/**
 * Respuesta paginada del backend: espejo de PaginaResponse<T> (Guía 07, 5.9).
 * Un tipo genérico: el mismo sirve para reservas, apartamentos o cualquier listado.
 */
export interface RespuestaPaginada<T> {
    contenido: T[];          // los elementos de esta página
    paginaActual: number;    // empieza en 0
    totalPaginas: number;
    totalElementos: number;
    tamanoPagina: number;
}

/** Todos los listados del proyecto usan 10 resultados por página (12.4). */
export const TAMANO_PAGINA = 10;

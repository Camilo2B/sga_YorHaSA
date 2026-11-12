// src/modelos/pagina.ts

/** Espejo de PaginaInfo del openapi: datos de la página que acompañan a cada listado. */
export interface InfoPagina {
    size: number;
    number: number;          // Página actual, empezando en 0
    totalElements: number;
    totalPages: number;
}

/** Formato de todos los listados de la API (10 por página). */
export interface Pagina<T> {
    content: T[];
    page: InfoPagina;
}

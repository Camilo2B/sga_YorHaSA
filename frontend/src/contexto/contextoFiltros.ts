import { createContext, type Dispatch } from 'react';
import type { EstadoReserva } from '../modelos/reserva';

export interface FiltrosReservas {
    estado: EstadoReserva | '';   // '' = todos los estados
    titular: string;              // búsqueda por nombre del titular (7.5)
    pagina: number;
}

export const FILTROS_INICIALES: FiltrosReservas = { estado: '', titular: '', pagina: 0 };

// Cada acción describe QUÉ pasó, no cómo cambia el estado: eso lo decide el reductor
export type AccionFiltros =
    | { tipo: 'cambiarEstado'; estado: EstadoReserva | '' }
    | { tipo: 'cambiarTitular'; titular: string }
    | { tipo: 'cambiarPagina'; pagina: number }
    | { tipo: 'limpiar' };

/**
 * Función pura: (estado actual, acción) → estado nuevo. No modifica el anterior.
 * La regla "un filtro nuevo vuelve a la página 0" vive aquí, en UN solo lugar,
 * y no repartida en cada manejador de eventos.
 */
export function reductorFiltros(filtros: FiltrosReservas, accion: AccionFiltros): FiltrosReservas {
    switch (accion.tipo) {
        case 'cambiarEstado':
            return { ...filtros, estado: accion.estado, pagina: 0 };
        case 'cambiarTitular':
            return { ...filtros, titular: accion.titular, pagina: 0 };
        case 'cambiarPagina':
            return { ...filtros, pagina: accion.pagina };
        case 'limpiar':
            return FILTROS_INICIALES;
    }
}

export interface ValorFiltros {
    filtros: FiltrosReservas;
    despachar: Dispatch<AccionFiltros>;
}

export const ContextoFiltros = createContext<ValorFiltros | null>(null);

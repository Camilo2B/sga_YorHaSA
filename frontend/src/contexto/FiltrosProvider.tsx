import { useMemo, useReducer, type ReactNode } from 'react';
import { ContextoFiltros, FILTROS_INICIALES, reductorFiltros } from './contextoFiltros';


/**
 * Estado global de los filtros del listado de reservas. Vive por encima de las
 * rutas: cuando el usuario sale del listado y vuelve, los filtros y la página siguen ahí.
 */
export function FiltrosProvider({ children }: { children: ReactNode }) {
    const [filtros, despachar] = useReducer(reductorFiltros, FILTROS_INICIALES);
    const valor = useMemo(() => ({ filtros, despachar }), [filtros]);
    return <ContextoFiltros value={valor}>{children}</ContextoFiltros>;
}

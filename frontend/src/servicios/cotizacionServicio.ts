// src/servicios/cotizacionServicio.ts
import type { CotizarEstanciaRequest, CotizacionResponse } from '../modelos/cotizacion';
import { api } from './api';

export async function cotizarEstancia(datos: CotizarEstanciaRequest): Promise<CotizacionResponse> {
    const respuesta = await api.post<CotizacionResponse>('/cotizaciones', datos);
    return respuesta.data;
}
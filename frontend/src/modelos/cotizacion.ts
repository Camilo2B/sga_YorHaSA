// src/modelos/cotizacion.ts
import type { OcupanteRequest } from './reserva';

/** Espejo de: record CotizarEstanciaRequest(...). Los ocupantes usan el mismo formato que la reserva. */
export interface CotizarEstanciaRequest {
    identificacionApartamento: string;
    fechaEntrada: string;
    fechaSalida: string;
    ocupantes: OcupanteRequest[];
}

/** Espejo parcial de: record CotizacionResponse(...). El desglose por noche aún no se muestra. */
export interface CotizacionResponse {
    identificacionApartamento: string;
    fechaEntrada: string;
    fechaSalida: string;
    noches: number;
    ocupantesFacturables: number;
    valorTotal: number;   // Pesos, sin decimales: long en Java
    moneda: string;       // Ej: "COP"
}

// src/modelos/cotizacion.ts

export interface CotizarEstanciaRequest {
    identificacionApartamento: string;
    fechaEntrada: string;
    fechaSalida: string;
    fechasNacimientoOcupantes: string[]; // Arreglo con las fechas de nacimiento "AAAA-MM-DD"
}

export interface CotizacionResponse {
    valorTotal: number;
    moneda: string; // Ej: "COP"
    noches: number;
}
// src/modelos/reserva.ts

/** Enums cerrados del dominio: viajan como texto y se tipan como uniones. */
export type EstadoReserva =
    | 'PENDIENTE'
    | 'CONFIRMADA'
    | 'EN_CURSO'
    | 'FINALIZADA'
    | 'CANCELADA'
    | 'NO_SHOW';

export type CanalOrigen = 'PORTAL' | 'DIRECTO' | 'EXTERNO';

/** Espejo de: record OcupanteRequest(String documento, String nombre, LocalDate fechaNacimiento) */
export interface OcupanteRequest {
    documento: string;
    nombre: string;
    fechaNacimiento: string;   // LocalDate viaja como "AAAA-MM-DD"
}

/** Espejo de: record CrearReservaRequest(...) */
export interface CrearReservaRequest {
    identificacionApartamento: string;
    fechaEntrada: string;
    fechaSalida: string;
    canalOrigen: CanalOrigen;
    horaEstimadaLlegada: string | null;   // LocalTime: "16:30". Opcional al crear (RN-09)
    titular: OcupanteRequest;
    ocupantes: OcupanteRequest[];         // Incluye al titular
}

/** Espejo de: record OcupanteResponse(String documento, String nombre, LocalDate fechaNacimiento) */
export interface OcupanteResponse {
    documento: string;
    nombre: string;
    fechaNacimiento: string;
}

/** Espejo de: record ReservaDetalleResponse(...) */
export interface ReservaDetalle {
    codigo: string;
    estado: EstadoReserva;
    canalOrigen: CanalOrigen;
    identificacionApartamento: string;
    fechaEntrada: string;
    fechaSalida: string;
    noches: number;
    horaEstimadaLlegada: string | null;
    titular: OcupanteResponse;
    ocupantes: OcupanteResponse[];
    totalOcupantes: number;
    valorTotal: number;                    // Pesos, sin decimales: long en Java
    moneda: string;                        // "COP"
    versionPoliticaCancelacion: string;
    fechaCreacion: string;
    motivoCancelacion: string | null;
}
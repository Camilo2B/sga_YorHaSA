// src/modelos/apartamento.ts

export type EstadoOperativo =
    | 'PREPARADO'
    | 'OCUPADO'
    | 'PENDIENTE_PREPARACION'
    | 'EN_PREPARACION'
    | 'FUERA_DE_SERVICIO';

/** Espejo de: record DormitorioResponse(String descripcion, String tipoCama) */
export interface Dormitorio {
    descripcion: string;
    tipoCama: string;
}

/** Espejo de: record ApartamentoResponse(...) */
export interface Apartamento {
    identificacion: string;   // "ATK-001": la identidad del negocio (L-04)
    nombre: string;
    capacidad: number;        // tope rígido de personas (RN-02)
    dormitorios: Dormitorio[];
    permiteMascotas: boolean;
    estadoOperativo: EstadoOperativo;
    activo: boolean;
}

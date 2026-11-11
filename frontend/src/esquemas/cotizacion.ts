// src/esquemas/cotizacion.ts
import { z } from 'zod';

export const esquemaCotizacion = z.object({
    identificacionApartamento: z.string().min(1, 'Debe indicar el apartamento'),
    fechaEntrada: z.string().min(1, 'La fecha de entrada es obligatoria'),
    fechaSalida: z.string().min(1, 'La fecha de salida es obligatoria'),
    fechasNacimientoOcupantes: z
        .array(
            z.object({
                fecha: z.string().min(1, 'La fecha de nacimiento es obligatoria'),
            })
        )
        .min(1, 'Debe incluir al menos un ocupante'),
});

export type FormularioCotizacion = z.infer<typeof esquemaCotizacion>;
// src/esquemas/cotizacion.ts
import { z } from 'zod';
import { esquemaOcupante } from './reserva';

export const esquemaCotizacion = z
    .object({
        identificacionApartamento: z.string().trim().min(1, 'Indique el apartamento'),
        fechaEntrada: z.iso.date('Ingrese la fecha de entrada'),
        fechaSalida: z.iso.date('Ingrese la fecha de salida'),
        ocupantes: z.array(esquemaOcupante).min(1, 'Debe incluir al menos un ocupante'),
    })
    // Ayuda para el usuario: la regla RN-03 sigue viviendo en el dominio (el backend responde 409).
    .refine((datos) => datos.fechaSalida > datos.fechaEntrada, {
        error: 'La fecha de salida debe ser posterior a la de entrada',
        path: ['fechaSalida'],
    });

export type FormularioCotizacion = z.infer<typeof esquemaCotizacion>;

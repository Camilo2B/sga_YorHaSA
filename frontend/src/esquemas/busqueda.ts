// src/esquemas/busqueda.ts
import { z } from 'zod';
import { hoyISO } from '../utilidades/fechas';

// Formulario de GET /api/apartamentos/disponibles (7.5). Los números se leen con valueAsNumber.
export const esquemaBusqueda = z
    .object({
        fechaEntrada: z
            .iso.date('Ingrese la fecha de entrada')
            .refine((fecha) => fecha >= hoyISO(), 'La fecha de entrada no puede ser anterior a hoy'),
        fechaSalida: z.iso.date('Ingrese la fecha de salida'),
        adultos: z
            .number('Indique el número de adultos')
            .int('Debe ser un número entero')
            .min(1, 'Debe haber al menos un adulto'),
        ninos: z
            .number('Indique el número de niños (0 si no hay)')
            .int('Debe ser un número entero')
            .min(0, 'No puede ser negativo'),
        conMascota: z.boolean(),
    })
    .refine((datos) => datos.fechaSalida > datos.fechaEntrada, {
        error: 'La fecha de salida debe ser posterior a la de entrada',
        path: ['fechaSalida'],
    });

export type FormularioBusqueda = z.infer<typeof esquemaBusqueda>;

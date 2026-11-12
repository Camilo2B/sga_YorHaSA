import { z } from 'zod';
import { hoyISO } from '../utilidades/fechas';

// Compartido con la cotización: ambos envían ocupantes con el mismo formato (OcupanteRequest).
export const esquemaOcupante = z.object({
    // Misma condición que OcupanteRequest del backend: letras y números, de 7 a 11 caracteres
    documento: z
        .string()
        .min(1, 'Ingrese el documento')
        .regex(/^[A-Za-z0-9]+$/, 'El documento solo admite letras y números')
        .min(7, 'El documento debe tener entre 7 y 11 caracteres')
        .max(11, 'El documento debe tener entre 7 y 11 caracteres'),
    nombre: z.string().trim().min(1, 'Ingrese el nombre'),
    fechaNacimiento: z
        .iso.date('Ingrese la fecha de nacimiento')
        .refine((fecha) => fecha < hoyISO(), 'La fecha de nacimiento no puede ser futura'),
});

export const esquemaNuevaReserva = z
    .object({
        identificacionApartamento: z.string().trim().min(1, 'Indique el apartamento'),
        fechaEntrada: z.iso.date('Ingrese la fecha de entrada'),
        fechaSalida: z.iso.date('Ingrese la fecha de salida'),
        // input type="time" entrega "" cuando está vacío: se acepta vacío o una hora válida
        horaEstimadaLlegada: z.union([z.iso.time('Hora inválida'), z.literal('')]),
        ocupantes: z.array(esquemaOcupante).min(1, 'La reserva debe tener al menos un ocupante'),
    })
    // Validación entre dos campos. Es una ayuda para el usuario: la regla RN-03
    // sigue viviendo en Estancia y el backend responde 409 si se viola.
    .refine((datos) => datos.fechaSalida > datos.fechaEntrada, {
        error: 'La fecha de salida debe ser posterior a la de entrada',
        path: ['fechaSalida'],
    });

export type FormularioNuevaReserva = z.infer<typeof esquemaNuevaReserva>;

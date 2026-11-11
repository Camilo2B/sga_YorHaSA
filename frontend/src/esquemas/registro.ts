// src/esquemas/registro.ts
import { z } from 'zod';

export const esquemaRegistro = z.object({
    nombre: z.string().min(1, 'El nombre es obligatorio'),
    correo: z.string().email('El correo no tiene un formato válido'),
    documento: z.string().min(1, 'El documento es obligatorio'),
    telefono: z.string().min(1, 'El teléfono es obligatorio'),
    fechaNacimiento: z.string().min(1, 'La fecha de nacimiento es obligatoria'),
    contrasena: z
        .string()
        .min(8, 'La contraseña debe tener al menos 8 caracteres')
        .regex(/[A-Z]/, 'Debe contener al menos una letra mayúscula')
        .regex(/[0-9]/, 'Debe contener al menos un número'),
    confirmarContrasena: z.string(),
}).refine((data) => data.contrasena === data.confirmarContrasena, {
    message: 'Las contraseñas no coinciden',
    path: ['confirmarContrasena'],
});

export type FormularioRegistro = z.infer<typeof esquemaRegistro>;
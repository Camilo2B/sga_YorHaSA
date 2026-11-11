// src/esquemas/login.ts
import { z } from 'zod';

export const esquemaLogin = z.object({
    correo: z.string().email('El correo no tiene un formato válido'),
    contrasena: z.string().min(1, 'La contraseña es obligatoria'),
});

export type FormularioLogin = z.infer<typeof esquemaLogin>;
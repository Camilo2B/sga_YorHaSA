// src/modelos/auth.ts

// Espejo del record LoginRequest de Java
export interface LoginRequest {
    correo: string;
    contrasena: string;
}

/**
 * Roles del SGA.
 * Se implementa como un tipo unión en lugar de un enum de TypeScript
 * debido a que la configuración del proyecto con Vite activa 'erasableSyntaxOnly'.
 */
export type Rol = 'HUESPED' | 'RECEPCIONISTA' | 'ADMINISTRADOR' | 'PERSONAL_SERVICIO';

// Espejo del record TokenResponse de Java (los nombres de campo deben ser idénticos)
export interface TokenResponse {
    token: string;
    tipo: string;       // Por lo general "Bearer"
    expiraEn: string;   // El Instant de Java llega como texto ISO-8601 (ej. "2026-11-20T15:30:00Z")
    roles: Rol[];
}

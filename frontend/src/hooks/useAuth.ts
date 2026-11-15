import { useContext } from 'react';
import { ContextoAuth } from '../contexto/contextoAuth';

export function useAuth() {
    const valor = useContext(ContextoAuth);
    if (!valor) {
        // Un error claro en lugar de un "Cannot read properties of null" más adelante
        throw new Error('useAuth debe usarse dentro de <AuthProvider>');
    }
    return valor;
}

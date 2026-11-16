import { useEffect, useState } from 'react';

/**
 * Devuelve el valor recibido, pero solo después de que deja de cambiar durante
 * `retardo` milisegundos. Mientras el usuario sigue escribiendo, devuelve el anterior.
 */
export function useDebounce<T>(valor: T, retardo = 400): T {
    const [valorEstable, setValorEstable] = useState(valor);

    useEffect(() => {
        const temporizador = setTimeout(() => setValorEstable(valor), retardo);
        // Si el valor cambia antes de cumplirse el retardo, se cancela el temporizador
        // anterior y empieza uno nuevo: solo el último sobrevive.
        return () => clearTimeout(temporizador);
    }, [valor, retardo]);

    return valorEstable;
}

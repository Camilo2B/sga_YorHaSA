// Un formateador se crea una vez y se reutiliza: construirlo es lo costoso.
const pesos = new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,   // el peso colombiano se maneja sin decimales (3.4)
});

const fechaLarga = new Intl.DateTimeFormat('es-CO', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
});

/** 480000 → "$ 480.000" */
export function formatearPesos(valor: number): string {
    return pesos.format(valor);
}

/**
 * "2026-11-20" → "20 de nov de 2026".
 * new Date("2026-11-20") se interpreta en UTC, y en Colombia (UTC-5) mostraría
 * el 19 de noviembre. Agregar la hora sin zona fuerza la interpretación local.
 */
export function formatearFecha(fechaISO: string): string {
    return fechaLarga.format(new Date(`${fechaISO}T00:00:00`));
}
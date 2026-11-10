import type { ApartamentoResumen } from '../modelos/apartamento';

// Datos de ejemplo mientras el frontend no consulta el backend.
// Reemplácelos por los apartamentos de su Ficha del Alojamiento (Anexo A).
export const apartamentosEjemplo: ApartamentoResumen[] = [
    { identificacion: 'ATK-001', nombre: 'Attack Suite', dormitorios: 2, capacidad: 4, caracteristicas: 'Zona de práctica de combate básica, dormitorios con cama doble, salacomedor rústica y cocina minimalista\n' },
    { identificacion: 'BTL-001', nombre: 'Battle Suite', dormitorios: 1, capacidad: 2, caracteristicas: 'Zona social amplia, decoración con réplicas de armamento táctico, consola de entretenimiento, terraza, un dormitorio con cama doble\n'},
    { identificacion: 'CTL-001', nombre: 'Controller Suite', dormitorios: 2, capacidad: 6, caracteristicas: 'Pantalla holográfica de proyección, iluminación LED blanca/negra, balcón panorámico, zona de trabajo ejecutiva. ' },
    { identificacion: 'DFD-001', nombre: 'Defender Suite', dormitorios: 2, capacidad: 4, caracteristicas: 'Zona de práctica de combate básica, 2 dormitorios con camas dobles, salacomedor y escaner de movimientos'},
    { identificacion: 'EXT-001', nombre: 'Executioner Suite', dormitorios: 3, capacidad: 6, caracteristicas: 'Aislamiento acústico de nivel militar, climatización de precisión, ambientación retro-futurista de red de datos. '},
    { identificacion: 'GNR-001', nombre: 'Gunner Suite', dormitorios: 1, capacidad: 2, caracteristicas: 'Sala de tiro básica, un dormitorio con cama doble, televisor curvo, salacomedor'}
];

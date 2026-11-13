import type { Apartamento } from '../modelos/apartamento';

// Datos de ejemplo mientras el frontend no consulta el backend.
export const apartamentosEjemplo: Apartamento[] = [
    {
        identificacion: 'ATK-001',
        nombre: 'Attack Suite',
        capacidad: 4,
        dormitorios: [
            { descripcion: 'Dormitorio 1', tipoCama: 'Doble' },
            { descripcion: 'Dormitorio 2', tipoCama: 'Doble' },
        ],
        permiteMascotas: false,
        estadoOperativo: 'PREPARADO',
        activo: true,
        caracteristicas: 'Zona de práctica de combate básica, dormitorios con cama doble, salacomedor rústica y cocina minimalista',
    },
    {
        identificacion: 'BTL-001',
        nombre: 'Battle Suite',
        capacidad: 2,
        dormitorios: [
            { descripcion: 'Dormitorio 1', tipoCama: 'Doble' },
        ],
        permiteMascotas: false,
        estadoOperativo: 'PREPARADO',
        activo: true,
        caracteristicas: 'Zona social amplia, decoración con réplicas de armamento táctico, consola de entretenimiento, terraza, un dormitorio con cama doble',
    },
    {
        identificacion: 'CTL-001',
        nombre: 'Controller Suite',
        capacidad: 6,
        dormitorios: [
            { descripcion: 'Dormitorio 1', tipoCama: 'Doble' },
            { descripcion: 'Dormitorio 2', tipoCama: 'Doble' },
        ],
        permiteMascotas: false,
        estadoOperativo: 'PREPARADO',
        activo: true,
        caracteristicas: 'Pantalla holográfica de proyección, iluminación LED blanca/negra, balcón panorámico, zona de trabajo ejecutiva',
    },
    {
        identificacion: 'DFD-001',
        nombre: 'Defender Suite',
        capacidad: 4,
        dormitorios: [
            { descripcion: 'Dormitorio 1', tipoCama: 'Doble' },
            { descripcion: 'Dormitorio 2', tipoCama: 'Doble' },
        ],
        permiteMascotas: false,
        estadoOperativo: 'PREPARADO',
        activo: true,
        caracteristicas: 'Zona de práctica de combate básica, 2 dormitorios con camas dobles, salacomedor y escáner de movimientos',
    },
    {
        identificacion: 'EXT-001',
        nombre: 'Executioner Suite',
        capacidad: 6,
        dormitorios: [
            { descripcion: 'Dormitorio 1', tipoCama: 'Doble' },
            { descripcion: 'Dormitorio 2', tipoCama: 'Doble' },
            { descripcion: 'Dormitorio 3', tipoCama: 'Doble' },
        ],
        permiteMascotas: false,
        estadoOperativo: 'PREPARADO',
        activo: true,
        caracteristicas: 'Aislamiento acústico de nivel militar, climatización de precisión, ambientación retro-futurista de red de datos',
    },
    {
        identificacion: 'GNR-001',
        nombre: 'Gunner Suite',
        capacidad: 2,
        dormitorios: [
            { descripcion: 'Dormitorio 1', tipoCama: 'Doble' },
        ],
        permiteMascotas: false,
        estadoOperativo: 'PREPARADO',
        activo: true,
        caracteristicas: 'Sala de tiro básica, un dormitorio con cama doble, televisor curvo, salacomedor',
    },
];
import type { EstadoReserva, ReservaResumen } from '../modelos/reserva';
import { formatearFecha, formatearPesos } from '../utilidades/formato';
import estilos from './TarjetaReserva.module.css';


// Record<EstadoReserva, string> obliga a cubrir los seis estados:
// si mañana aparece uno nuevo, TypeScript señala que falta aquí.
const COLOR_ESTADO: Record<EstadoReserva, string> = {
    PENDIENTE: 'text-bg-warning',
    CONFIRMADA: 'text-bg-primary',
    EN_CURSO: 'text-bg-success',
    FINALIZADA: 'text-bg-secondary',
    CANCELADA: 'text-bg-danger',
    NO_SHOW: 'text-bg-dark',
};


const BORDE_ESTADO: Partial<Record<EstadoReserva, string>> = {
    PENDIENTE: estilos.pendiente,
    CONFIRMADA: estilos.confirmada,
    EN_CURSO: estilos.enCurso,
};


interface TarjetaReservaProps {
    reserva: ReservaResumen;
}


export function TarjetaReserva({ reserva }: TarjetaReservaProps) {
    const terminada = ['FINALIZADA', 'CANCELADA', 'NO_SHOW'].includes(reserva.estado);


    // Varias clases condicionales en un solo className
    const clases = [
        'card h-100',
        estilos.tarjeta,
        BORDE_ESTADO[reserva.estado] ?? '',
        terminada ? estilos.terminada : '',
    ].join(' ');


    return (
        <article className={clases}>
            <div className="card-header d-flex justify-content-between align-items-center">
                <span className="fw-semibold">{reserva.codigo}</span>
                <span className={`badge ${COLOR_ESTADO[reserva.estado]}`}>{reserva.estado}</span>
            </div>
            <div className="card-body">
                <p className="mb-1"><i className="bi bi-person me-1" aria-hidden="true"></i>{reserva.nombreTitular}</p>
                <p className="mb-1 text-body-secondary small">
                    <i className="bi bi-building me-1" aria-hidden="true"></i>{reserva.identificacionApartamento}
                    <span className="mx-2">·</span>
                    {reserva.totalOcupantes} ocupante(s)
                </p>
                <p className="mb-0 text-body-secondary small">
                    <i className="bi bi-calendar-range me-1" aria-hidden="true"></i>
                    {formatearFecha(reserva.fechaEntrada)} → {formatearFecha(reserva.fechaSalida)}
                </p>
            </div>
            <div className="card-footer d-flex justify-content-between">
                <small className="text-body-secondary">{reserva.canalOrigen}</small>
                <strong>{formatearPesos(reserva.valorTotal)}</strong>
            </div>
        </article>
    );
}

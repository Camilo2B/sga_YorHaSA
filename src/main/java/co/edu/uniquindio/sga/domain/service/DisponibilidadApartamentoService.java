package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;

import java.util.List;
import java.util.Objects;

/**
 * Servicio de Dominio que coordina la verificación de disponibilidad de apartamentos
 * cruzando los bloqueos propios del Apartamento con las reservas activas registradas en el ReservaRepository.
 */
public class DisponibilidadApartamentoService {

    private final ApartamentoRepository apartamentoRepository;
    private final ReservaRepository reservaRepository;

    public DisponibilidadApartamentoService(ApartamentoRepository apartamentoRepository, ReservaRepository reservaRepository) {
        this.apartamentoRepository = Objects.requireNonNull(apartamentoRepository, "El repositorio de apartamentos no puede ser nulo");
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "El repositorio de reservas no puede ser nulo");
    }

    /**
     * Evalúa si un apartamento específico está disponible para alojar a un número de personas en una estancia determinada.
     */
    public boolean estaDisponible(String apartamentoId, Estancia estancia, int totalOcupantes) {
        Apartamento apartamento = apartamentoRepository.buscarPorId(apartamentoId)
                .orElseThrow(() -> new ReglaDominioException("No se encontró el apartamento con ID: " + apartamentoId));

        // Obtener estancias de reservas activas o vigentes para ese apartamento
        List<Estancia> estanciasReservadas = reservaRepository.buscarPorApartamento(apartamento.identificacion()).stream()
                .filter(r -> r.getEstado() == EstadoReserva.PENDIENTE || r.getEstado() == EstadoReserva.CONFIRMADA || r.getEstado() == EstadoReserva.EN_CURSO)
                .map(Reserva::getEstancia)
                .toList();

        return apartamento.estaDisponible(estancia, totalOcupantes, estanciasReservadas);
    }
}
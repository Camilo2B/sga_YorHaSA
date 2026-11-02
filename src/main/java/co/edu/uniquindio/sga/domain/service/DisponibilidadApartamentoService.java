package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosAlojamiento;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Servicio de Dominio que coordina la verificación de disponibilidad de apartamentos cruzando los bloqueos y la
 * capacidad del Apartamento con las reservas activas registradas en el ReservaRepository (RN-01, RN-02, RN-07,
 * RN-20 y RN-Propia-03).
 */
public class DisponibilidadApartamentoService {

    private final ApartamentoRepository apartamentoRepository;
    private final ReservaRepository reservaRepository;

    public DisponibilidadApartamentoService(ApartamentoRepository apartamentoRepository, ReservaRepository reservaRepository) {
        this.apartamentoRepository = Objects.requireNonNull(apartamentoRepository, "El repositorio de apartamentos no puede ser nulo");
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "El repositorio de reservas no puede ser nulo");
    }

    /**
     * Evalúa si un apartamento puede alojar al grupo (personas y, si la trae, la mascota) en una estancia.
     * RN-20: si el tiempo de preparación no cabe entre la hora de salida y la de entrada, una reserva que termina el
     * día en que empieza la estancia (o que empieza el día en que termina) también la impide.
     */
    public boolean estaDisponible(Apartamento apartamento, Estancia estancia, int totalOcupantes, boolean conMascota,
                                  ParametrosAlojamiento parametros) {
        Objects.requireNonNull(apartamento, "El apartamento no puede ser nulo");
        Objects.requireNonNull(estancia, "La estancia no puede ser nula");
        Objects.requireNonNull(parametros, "Los parámetros del alojamiento no pueden ser nulos");

        boolean permiteRecambioMismoDia = parametros.horario().permiteRecambioMismoDia(parametros.tiempoPreparacion());
        // Sin recambio el mismo día también cuentan las reservas contiguas: se consulta un día más a cada lado.
        Estancia consulta = permiteRecambioMismoDia
                ? estancia
                : new Estancia(estancia.entrada().minusDays(1), estancia.salida().plusDays(1));

        // Solo las reservas activas (RN-12: las canceladas y los no-show ya liberaron sus noches)
        List<Estancia> estanciasReservadas = reservaRepository.buscarActivasPorApartamento(apartamento.identificacion(), consulta)
                .stream()
                .map(Reserva::getEstancia)
                .toList();

        return apartamento.estaDisponible(estancia, totalOcupantes, conMascota, estanciasReservadas, permiteRecambioMismoDia);
    }

    /** 7.5: apartamentos disponibles para la estancia y la composición del grupo, ordenados por identificación. */
    public List<Apartamento> buscarDisponibles(Estancia estancia, int totalOcupantes, boolean conMascota,
                                               ParametrosAlojamiento parametros) {
        return apartamentoRepository.obtenerTodos().stream()
                .filter(apartamento -> estaDisponible(apartamento, estancia, totalOcupantes, conMascota, parametros))
                .sorted(Comparator.comparing(Apartamento::identificacion))
                .toList();
    }
}

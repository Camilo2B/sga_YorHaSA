package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;

import java.util.Objects;

/**
 * Servicio de Dominio para los bloqueos administrativos (RN-07). 7.3: no puede registrarse un bloqueo sobre noches
 * que ya tengan reservas activas. Es servicio y no método de Apartamento porque las reservas son otro agregado.
 * Clase Java pura: se registra como bean en DomainServiceConfig.
 */
public class GestionBloqueoService {

    private final ReservaRepository reservaRepository;

    public GestionBloqueoService(ReservaRepository reservaRepository) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "El repositorio de reservas no puede ser nulo");
    }

    /**
     * Agrega el bloqueo al apartamento verificando antes que no existan reservas activas en esas noches. Los bloqueos
     * que se solapan entre sí los rechaza el propio Apartamento. Quien invoca guarda el apartamento.
     */
    public void registrarBloqueo(Apartamento apartamento, Bloqueo bloqueo) {
        Objects.requireNonNull(apartamento, "El apartamento no puede ser nulo");
        Objects.requireNonNull(bloqueo, "El bloqueo no puede ser nulo");

        boolean hayReservaConflicto = !reservaRepository.buscarActivasPorApartamento(apartamento.identificacion(), bloqueo.rango()).isEmpty();
        if (hayReservaConflicto) {
            throw new ReglaDominioException("RN-07 / 7.3: no se puede bloquear el apartamento " + apartamento.identificacion()
                    + " del " + bloqueo.rango().entrada() + " al " + bloqueo.rango().salida()
                    + ": existen reservas activas en esas noches");
        }
        apartamento.agregarBloqueo(bloqueo);
    }
}

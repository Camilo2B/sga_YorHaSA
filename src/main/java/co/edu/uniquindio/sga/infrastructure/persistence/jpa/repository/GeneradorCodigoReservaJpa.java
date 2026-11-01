package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Genera RES-AAAA-NNNNN: el año sale del reloj y el consecutivo es la cantidad de reservas de ese año más una. Como
 * nunca se borran reservas físicamente, el consecutivo no se repite; la restricción única de la columna codigo es la
 * red de seguridad si dos solicitudes simultáneas calculan el mismo número.
 */
@Component
public class GeneradorCodigoReservaJpa implements GeneradorCodigoReserva {

    private final ReservaJpaDataRepository dataRepository;
    private final Clock clock;

    public GeneradorCodigoReservaJpa(ReservaJpaDataRepository dataRepository, Clock clock) {
        this.dataRepository = dataRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public synchronized CodigoReserva generar() {
        int anio = LocalDate.now(clock).getYear();
        long consecutivo = dataRepository.countByCodigoStartingWith("RES-" + anio + "-") + 1;
        return CodigoReserva.de(anio, consecutivo);
    }
}

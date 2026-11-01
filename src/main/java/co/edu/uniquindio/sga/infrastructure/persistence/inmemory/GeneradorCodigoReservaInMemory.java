package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

import java.util.concurrent.atomic.AtomicLong;

/** Generador en memoria: solo para pruebas unitarias. La aplicación usa GeneradorCodigoReservaJpa. */
public class GeneradorCodigoReservaInMemory implements GeneradorCodigoReserva {

    private final int anio;
    private final AtomicLong consecutivo = new AtomicLong();

    public GeneradorCodigoReservaInMemory(int anio) {
        this.anio = anio;
    }

    @Override
    public CodigoReserva generar() {
        return CodigoReserva.de(anio, consecutivo.incrementAndGet());
    }
}

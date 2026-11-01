package co.edu.uniquindio.sga.infrastructure.scheduling;

import co.edu.uniquindio.sga.application.usecase.DeclararNoShowsVencidosUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Pasa automáticamente a NO_SHOW las reservas CONFIRMADA cuyo titular no llegó a tiempo (RN-12). La frecuencia se cambia en application.properties. */
@Component
public class NoShowScheduler {

    private static final Logger log = LoggerFactory.getLogger(NoShowScheduler.class);

    private final DeclararNoShowsVencidosUseCase declararNoShowsVencidosUseCase;

    public NoShowScheduler(DeclararNoShowsVencidosUseCase declararNoShowsVencidosUseCase) {
        this.declararNoShowsVencidosUseCase = declararNoShowsVencidosUseCase;
    }

    @Scheduled(fixedDelayString = "${sga.reservas.verificacion-no-show-ms:60000}",
            initialDelayString = "${sga.reservas.verificacion-no-show-ms:60000}")
    public void declararNoShows() {
        try {
            int declaradas = declararNoShowsVencidosUseCase.ejecutar();
            if (declaradas > 0) {
                log.info("RN-12: {} reservas pasaron a NO_SHOW por no llegar antes de la hora límite", declaradas);
            }
        } catch (RuntimeException e) {
            log.error("No se pudo revisar las reservas con la hora límite de no-show vencida", e);
        }
    }
}

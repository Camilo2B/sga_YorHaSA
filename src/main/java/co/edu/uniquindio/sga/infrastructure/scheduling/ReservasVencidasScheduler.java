package co.edu.uniquindio.sga.infrastructure.scheduling;

import co.edu.uniquindio.sga.application.usecase.CancelarReservasVencidasUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Revisa cada cierto tiempo las reservas PENDIENTE vencidas (RN-21). La frecuencia se cambia en application.properties. */
@Component
public class ReservasVencidasScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservasVencidasScheduler.class);

    private final CancelarReservasVencidasUseCase cancelarReservasVencidasUseCase;

    public ReservasVencidasScheduler(CancelarReservasVencidasUseCase cancelarReservasVencidasUseCase) {
        this.cancelarReservasVencidasUseCase = cancelarReservasVencidasUseCase;
    }

    @Scheduled(fixedDelayString = "${sga.reservas.verificacion-vencimiento-ms:60000}",
            initialDelayString = "${sga.reservas.verificacion-vencimiento-ms:60000}")
    public void cancelarVencidas() {
        try {
            int canceladas = cancelarReservasVencidasUseCase.ejecutar();
            if (canceladas > 0) {
                log.info("RN-21: se cancelaron {} reservas PENDIENTE con el plazo de confirmación vencido", canceladas);
            }
        } catch (RuntimeException e) {
            log.error("No se pudo revisar las reservas con el plazo de confirmación vencido", e);
        }
    }
}

package co.edu.uniquindio.sga.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Activa las tareas programadas (@Scheduled), como la cancelación de reservas vencidas (RN-21). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}

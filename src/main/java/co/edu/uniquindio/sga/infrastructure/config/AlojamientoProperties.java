package co.edu.uniquindio.sga.infrastructure.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * Parámetros de la Ficha (sección 6 / A.4) tal como están en application.properties (prefijo sga.alojamiento).
 * Las horas llegan como texto "HH:mm" y se convierten en DomainServiceConfig.
 * Si falta alguna propiedad o tiene un formato inválido, la aplicación no arranca y el mensaje indica cuál es.
 * Se usan tipos envoltorio (Integer, Boolean) para poder distinguir "no definido" de 0 o false.
 */
@Validated
@ConfigurationProperties(prefix = "sga.alojamiento")
public record AlojamientoProperties(
        @NotNull @Min(0) Integer umbralEdadFacturable,
        @NotBlank @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "debe tener formato HH:mm") String horaEntrada,
        @NotBlank @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "debe tener formato HH:mm") String horaSalida,
        @NotNull @Min(0) Integer tiempoPreparacionHoras,
        @NotNull @Min(0) Integer plazoConfirmacionHoras,
        @NotBlank @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "debe tener formato HH:mm") String horaLimiteNoShow,
        @NotNull Boolean exigeAnticipo,
        @NotNull BigDecimal porcentajeAnticipo) {
}
package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Datos de prueba (12.3): al arrancar, si todavía no existe el alojamiento, lo crea con los datos de la Ficha
 * (A.1 y política de cancelación de A.4). Los PARÁMETROS no se cargan aquí: vienen de application.properties.
 * Más adelante este cargador también creará las temporadas, los apartamentos con sus tarifas, usuarios y reservas.
 */
@Component
public class AlojamientoInicializador implements ApplicationRunner {

    private static final String NOMBRE = "Búnker Orbital YoRHa - Suites & Apartamentos Temáticos";
    private static final String CIUDAD = "El Espacio";

    /** Política v1 de la Ficha: más de 7 días no retiene, entre 3 y 7 días retiene 50%, menos de 3 días o no-show retiene 100%. */
    private static final List<TramoAntelacion> TRAMOS_POLITICA_INICIAL = List.of(
            new TramoAntelacion(7 * 24, BigDecimal.ZERO),
            new TramoAntelacion(3 * 24, new BigDecimal("0.50")),
            new TramoAntelacion(0, BigDecimal.ONE));

    private final AlojamientoRepository alojamientoRepository;
    private final ParametrosAlojamiento parametros;
    private final Clock clock;

    public AlojamientoInicializador(AlojamientoRepository alojamientoRepository, ParametrosAlojamiento parametros, Clock clock) {
        this.alojamientoRepository = alojamientoRepository;
        this.parametros = parametros;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (alojamientoRepository.obtener().isPresent()) {
            return;
        }
        alojamientoRepository.guardar(Alojamiento.crear(NOMBRE, CIUDAD, parametros,
                Temporada.crearBase("Base"), TRAMOS_POLITICA_INICIAL, LocalDate.now(clock)));
    }
}

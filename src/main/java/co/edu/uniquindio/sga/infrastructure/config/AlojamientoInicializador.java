package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Datos de prueba (12.3): al arrancar carga solo lo que falte de la Ficha: el alojamiento (A.1) con su temporada base
 * y la política de cancelación v1 (A.4), las temporadas Media y Alta (A.3) y los 15 apartamentos del inventario (A.2)
 * con una tarifa por cada temporada (A.3: toda combinación apartamento × temporada tiene tarifa).
 * Los PARÁMETROS no se cargan aquí: vienen de application.properties. Cada paso consulta antes de crear, así que
 * arrancar varias veces no duplica nada.
 */
@Component
public class AlojamientoInicializador implements ApplicationRunner {

    private static final String NOMBRE = "Búnker Orbital YoRHa - Suites & Apartamentos Temáticos";
    private static final String CIUDAD = "El Espacio";

    private static final String NOMBRE_TEMPORADA_BASE = "Base";
    private static final String NOMBRE_TEMPORADA_MEDIA = "Media (Sincronización)";
    private static final String NOMBRE_TEMPORADA_ALTA = "Alta (Despliegue de Misión)";

    /**
     * La Ficha describe las temporadas de forma general (recesos, mitad de año, Semana Santa, fin de año). Aquí se carga
     * un rango concreto de cada una para poder probar; el resto de rangos se registran por la API.
     */
    private static final LocalDate MEDIA_DESDE = LocalDate.of(2026, 11, 1);
    private static final LocalDate MEDIA_HASTA = LocalDate.of(2026, 12, 15);
    private static final LocalDate ALTA_DESDE = LocalDate.of(2026, 12, 15);
    private static final LocalDate ALTA_HASTA = LocalDate.of(2027, 1, 16);

    /** Las tarifas de la Ficha rigen desde el inicio del año de operación. */
    private static final LocalDate TARIFAS_VIGENTES_DESDE = LocalDate.of(2026, 1, 1);

    /** Política v1 de la Ficha: más de 7 días no retiene, entre 3 y 7 días retiene 50%, menos de 3 días o no-show retiene 100%. */
    private static final List<TramoAntelacion> TRAMOS_POLITICA_INICIAL = List.of(
            new TramoAntelacion(7 * 24, BigDecimal.ZERO),
            new TramoAntelacion(3 * 24, new BigDecimal("0.50")),
            new TramoAntelacion(0, BigDecimal.ONE));

    /** Fila del inventario (A.2) con sus tarifas (A.3): id, nombre, dormitorios, capacidad, tarifa base, media y alta. */
    private record FichaApartamento(String id, String nombre, int dormitorios, int capacidad,
                                    long tarifaBase, long tarifaMedia, long tarifaAlta) {
        boolean esExclusive() {
            return nombre.endsWith("Exclusive");
        }
    }

    private static final List<FichaApartamento> INVENTARIO = List.of(
            new FichaApartamento("ATK-001", "Attack Suite", 2, 4, 80_000, 100_000, 125_000),
            new FichaApartamento("ATK-002", "Attack Suite Exclusive", 4, 8, 110_000, 135_000, 165_000),
            new FichaApartamento("BTL-001", "Battle Suite", 1, 2, 90_000, 110_000, 140_000),
            new FichaApartamento("BTL-002", "Battle Suite Exclusive", 3, 6, 120_000, 145_000, 180_000),
            new FichaApartamento("CTL-001", "Controller Suite", 2, 4, 95_000, 115_000, 145_000),
            new FichaApartamento("CTL-002", "Controller Suite Exclusive", 4, 8, 130_000, 160_000, 200_000),
            new FichaApartamento("DFD-001", "Defender Suite", 2, 4, 85_000, 105_000, 130_000),
            new FichaApartamento("DFD-002", "Defender Suite Exclusive", 4, 8, 115_000, 140_000, 170_000),
            new FichaApartamento("EXT-001", "Executioner Suite", 3, 6, 105_000, 130_000, 160_000),
            new FichaApartamento("GNR-001", "Gunner Suite", 1, 2, 85_000, 105_000, 130_000),
            new FichaApartamento("GNR-002", "Gunner Suite Exclusive", 3, 6, 115_000, 140_000, 175_000),
            new FichaApartamento("HLR-001", "Healer Suite", 1, 2, 90_000, 110_000, 135_000),
            new FichaApartamento("HLR-002", "Healer Suite Exclusive", 3, 6, 120_000, 145_000, 180_000),
            new FichaApartamento("OPR-001", "Operator Suite", 1, 2, 80_000, 100_000, 120_000),
            new FichaApartamento("SNR-001", "Scanner Suite", 2, 4, 100_000, 120_000, 150_000));

    private final AlojamientoRepository alojamientoRepository;
    private final ApartamentoRepository apartamentoRepository;
    private final ParametrosAlojamiento parametros;
    private final Clock clock;

    public AlojamientoInicializador(AlojamientoRepository alojamientoRepository,
                                    ApartamentoRepository apartamentoRepository,
                                    ParametrosAlojamiento parametros,
                                    Clock clock) {
        this.alojamientoRepository = alojamientoRepository;
        this.apartamentoRepository = apartamentoRepository;
        this.parametros = parametros;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        Alojamiento alojamiento = alojamientoRepository.obtener().orElseGet(() -> Alojamiento.crear(NOMBRE, CIUDAD, parametros,
                Temporada.crearBase(NOMBRE_TEMPORADA_BASE), TRAMOS_POLITICA_INICIAL, LocalDate.now(clock)));

        agregarTemporadaSiFalta(alojamiento, NOMBRE_TEMPORADA_MEDIA, MEDIA_DESDE, MEDIA_HASTA);
        agregarTemporadaSiFalta(alojamiento, NOMBRE_TEMPORADA_ALTA, ALTA_DESDE, ALTA_HASTA);
        alojamientoRepository.guardar(alojamiento);

        cargarApartamentos(alojamiento);
    }

    private static void agregarTemporadaSiFalta(Alojamiento alojamiento, String nombre, LocalDate desde, LocalDate hasta) {
        boolean existe = alojamiento.temporadas().stream().anyMatch(t -> t.nombre().equals(nombre));
        if (!existe) {
            alojamiento.agregarTemporada(Temporada.crear(nombre, desde, hasta));
        }
    }

    private void cargarApartamentos(Alojamiento alojamiento) {
        UUID base = temporada(alojamiento, NOMBRE_TEMPORADA_BASE);
        UUID media = temporada(alojamiento, NOMBRE_TEMPORADA_MEDIA);
        UUID alta = temporada(alojamiento, NOMBRE_TEMPORADA_ALTA);

        for (FichaApartamento ficha : INVENTARIO) {
            if (apartamentoRepository.buscarPorId(ficha.id()).isPresent()) {
                continue;
            }
            Apartamento apartamento = Apartamento.crear(ficha.id(), ficha.nombre(), ficha.capacidad(),
                    dormitorios(ficha), EstadoOperativo.PREPARADO);
            apartamento.definirTarifa(base, Dinero.de(ficha.tarifaBase()), TARIFAS_VIGENTES_DESDE);
            apartamento.definirTarifa(media, Dinero.de(ficha.tarifaMedia()), TARIFAS_VIGENTES_DESDE);
            apartamento.definirTarifa(alta, Dinero.de(ficha.tarifaAlta()), TARIFAS_VIGENTES_DESDE);
            apartamentoRepository.guardar(apartamento);
        }
    }

    private static UUID temporada(Alojamiento alojamiento, String nombre) {
        return alojamiento.temporadas().stream()
                .filter(t -> t.nombre().equals(nombre))
                .findFirst()
                .orElseThrow()
                .id();
    }

    /** La Ficha solo da el número de dormitorios; las Exclusive hablan de camas King y el resto de camas dobles. */
    private static List<Dormitorio> dormitorios(FichaApartamento ficha) {
        String tipoCama = ficha.esExclusive() ? "Cama King" : "Cama doble";
        List<Dormitorio> dormitorios = new ArrayList<>();
        for (int i = 1; i <= ficha.dormitorios(); i++) {
            dormitorios.add(new Dormitorio("Dormitorio " + i, tipoCama));
        }
        return dormitorios;
    }
}

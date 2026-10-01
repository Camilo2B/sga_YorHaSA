package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * F-13 - El sistema administra un único alojamiento. Es la raíz del agregado
 * que compone el historial de Temporada y de PoliticaCancelacion, y guarda
 * los parámetros de configuración de la sección 6 / Anexo A.4 (nunca
 * quemados en código: umbral de edad, horas, tiempos, anticipo).
 * <p>
 * Apartamento es una raíz de agregado DISTINTA (ver clase Apartamento):
 * Alojamiento la referencia solo por su identificador, nunca por objeto.
 */
public final class Alojamiento {

    private final UUID id;
    private final String nombre;
    private final String ciudad;
    private final int umbralEdadFacturable;
    private final LocalTime horaEntrada;
    private final LocalTime horaSalida;
    private final int tiempoPreparacionHoras;
    private final int plazoConfirmacionHoras;
    private final LocalTime horaLimiteNoShow;
    private final boolean exigeAnticipo;
    private final BigDecimal porcentajeAnticipo; // fracción (0.5 = 50%), no un Dinero: es proporcional al valor de cada estancia

    private final List<Temporada> temporadas = new ArrayList<>();
    private final List<PoliticaCancelacion> historialPoliticas = new ArrayList<>();

    private Alojamiento(UUID id, String nombre, String ciudad, int umbralEdadFacturable,
                        LocalTime horaEntrada, LocalTime horaSalida, int tiempoPreparacionHoras,
                        int plazoConfirmacionHoras, LocalTime horaLimiteNoShow,
                        boolean exigeAnticipo, BigDecimal porcentajeAnticipo,
                        Temporada temporadaBase, PoliticaCancelacion politicaInicial) {
        this.id = id;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.umbralEdadFacturable = umbralEdadFacturable;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.tiempoPreparacionHoras = tiempoPreparacionHoras;
        this.plazoConfirmacionHoras = plazoConfirmacionHoras;
        this.horaLimiteNoShow = horaLimiteNoShow;
        this.exigeAnticipo = exigeAnticipo;
        this.porcentajeAnticipo = porcentajeAnticipo;
        this.temporadas.add(temporadaBase);
        this.historialPoliticas.add(politicaInicial);
    }

    /**
     * F-07 y 7.4 exigen que exista, desde el primer momento, una política de
     * cancelación vigente y una temporada base — por eso son parámetros
     * obligatorios de creación, no algo que se agregue después "si acaso".
     */
    public static Alojamiento crear(String nombre, String ciudad, int umbralEdadFacturable,
                                    LocalTime horaEntrada, LocalTime horaSalida, int tiempoPreparacionHoras,
                                    int plazoConfirmacionHoras, LocalTime horaLimiteNoShow,
                                    boolean exigeAnticipo, BigDecimal porcentajeAnticipo,
                                    Temporada temporadaBase, List<TramoAntelacion> tramosPoliticaInicial,
                                    LocalDate vigenteDesde) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El nombre del alojamiento no puede estar vacío");
        }
        if (ciudad == null || ciudad.isBlank()) {
            throw new ReglaDominioException("La ciudad del alojamiento no puede estar vacía");
        }
        if (umbralEdadFacturable < 0) {
            throw new ReglaDominioException("El umbral de edad facturable no puede ser negativo");
        }
        Objects.requireNonNull(horaEntrada, "La hora de entrada no puede ser nula");
        Objects.requireNonNull(horaSalida, "La hora de salida no puede ser nula");
        if (tiempoPreparacionHoras < 0) {
            throw new ReglaDominioException("El tiempo de preparación no puede ser negativo");
        }
        if (plazoConfirmacionHoras < 0) {
            throw new ReglaDominioException("El plazo de confirmación no puede ser negativo");
        }
        Objects.requireNonNull(horaLimiteNoShow, "La hora límite de no-show no puede ser nula");
        if (exigeAnticipo) {
            Objects.requireNonNull(porcentajeAnticipo, "Si se exige anticipo, el porcentaje no puede ser nulo");
            if (porcentajeAnticipo.compareTo(BigDecimal.ZERO) <= 0 || porcentajeAnticipo.compareTo(BigDecimal.ONE) > 0) {
                throw new ReglaDominioException("El porcentaje de anticipo debe estar entre 0 (exclusivo) y 1");
            }
        }
        Objects.requireNonNull(temporadaBase, "El alojamiento debe crearse con una temporada base");
        if (!temporadaBase.esBase()) {
            throw new ReglaDominioException("La temporada inicial del alojamiento debe ser la temporada base");
        }
        PoliticaCancelacion politicaInicial = PoliticaCancelacion.crear(1, vigenteDesde, tramosPoliticaInicial);

        return new Alojamiento(UUID.randomUUID(), nombre, ciudad, umbralEdadFacturable, horaEntrada, horaSalida,
                tiempoPreparacionHoras, plazoConfirmacionHoras, horaLimiteNoShow, exigeAnticipo,
                exigeAnticipo ? porcentajeAnticipo : BigDecimal.ZERO, temporadaBase, politicaInicial);
    }

    /**
     * 7.4: las temporadas no pueden solaparse entre sí; solo puede existir
     * una temporada base.
     */
    public void agregarTemporada(Temporada nueva) {
        Objects.requireNonNull(nueva, "La temporada no puede ser nula");
        if (nueva.esBase()) {
            throw new ReglaDominioException("Ya existe una temporada base; no puede haber una segunda");
        }
        boolean solapa = temporadas.stream().anyMatch(t -> t.seSolapaCon(nueva));
        if (solapa) {
            throw new ReglaDominioException("La temporada '" + nueva.nombre() + "' se solapa con una temporada existente");
        }
        temporadas.add(nueva);
    }

    /**
     * 7.4: ninguna fecha reservable queda sin temporada. Se busca primero
     * entre las temporadas específicas; si ninguna cubre la fecha, se usa la
     * base como respaldo.
     */
    public Temporada temporadaVigente(LocalDate fecha) {
        Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        return temporadas.stream()
                .filter(t -> !t.esBase())
                .filter(t -> t.cubre(fecha))
                .findFirst()
                .orElseGet(() -> temporadas.stream()
                        .filter(Temporada::esBase)
                        .findFirst()
                        .orElseThrow(() -> new ReglaDominioException("El alojamiento no tiene temporada base configurada")));
    }

    /**
     * 7.4 / F-07: al actualizarla se conserva la versión anterior; nunca se
     * edita una PoliticaCancelacion existente, siempre se agrega una nueva.
     */
    public PoliticaCancelacion definirNuevaPolitica(List<TramoAntelacion> tramos, LocalDate vigenteDesde) {
        int siguienteVersion = historialPoliticas.size() + 1;
        PoliticaCancelacion nueva = PoliticaCancelacion.crear(siguienteVersion, vigenteDesde, tramos);
        historialPoliticas.add(nueva);
        return nueva;
    }

    /**
     * 3.5: la reserva congela la versión vigente al momento de crearse.
     */
    public PoliticaCancelacion politicaVigente(LocalDate fecha) {
        Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        return historialPoliticas.stream()
                .filter(p -> !p.vigenteDesde().isAfter(fecha))
                .max(Comparator.comparing(PoliticaCancelacion::vigenteDesde))
                .orElseThrow(() -> new ReglaDominioException("No hay una política de cancelación vigente para la fecha " + fecha));
    }

    public UUID id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public String ciudad() {
        return ciudad;
    }

    public int umbralEdadFacturable() {
        return umbralEdadFacturable;
    }

    public LocalTime horaEntrada() {
        return horaEntrada;
    }

    public LocalTime horaSalida() {
        return horaSalida;
    }

    public int tiempoPreparacionHoras() {
        return tiempoPreparacionHoras;
    }

    public int plazoConfirmacionHoras() {
        return plazoConfirmacionHoras;
    }

    public LocalTime horaLimiteNoShow() {
        return horaLimiteNoShow;
    }

    public boolean exigeAnticipo() {
        return exigeAnticipo;
    }

    public BigDecimal porcentajeAnticipo() {
        return porcentajeAnticipo;
    }

    public List<Temporada> temporadas() {
        return List.copyOf(temporadas);
    }

    public List<PoliticaCancelacion> historialPoliticas() {
        return List.copyOf(historialPoliticas);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alojamiento otro)) return false;
        return this.id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
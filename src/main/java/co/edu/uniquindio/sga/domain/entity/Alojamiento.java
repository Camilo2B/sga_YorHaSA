package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * F-13: el sistema administra un único alojamiento. Raíz del agregado que compone el historial de Temporada y de
 * PoliticaCancelacion, y "tiene" los parámetros de la Ficha (ParametrosAlojamiento), que le llegan ya armados:
 * el dominio no sabe que hoy vienen de application.properties.
 * <p>
 * Apartamento es una raíz de agregado DISTINTA: Alojamiento no la contiene ni la referencia por objeto.
 */
public final class Alojamiento {

    private final UUID id;
    private final String nombre;
    private final String ciudad;
    private final ParametrosAlojamiento parametros;
    private final List<Temporada> temporadas = new ArrayList<>();
    private final List<PoliticaCancelacion> historialPoliticas = new ArrayList<>();

    private Alojamiento(UUID id, String nombre, String ciudad, ParametrosAlojamiento parametros) {
        this.id = id;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.parametros = parametros;
    }

    /**
     * F-07 y 7.4 exigen que exista, desde el primer momento, una política de cancelación vigente y una temporada
     * base: por eso son obligatorias al crear, no algo que se agregue después "si acaso".
     */
    public static Alojamiento crear(String nombre, String ciudad, ParametrosAlojamiento parametros,
                                    Temporada temporadaBase, List<TramoAntelacion> tramosPoliticaInicial,
                                    LocalDate vigenteDesde) {
        validarIdentidad(nombre, ciudad);
        Objects.requireNonNull(parametros, "Los parámetros del alojamiento no pueden ser nulos");
        Objects.requireNonNull(temporadaBase, "El alojamiento debe crearse con una temporada base");
        if (!temporadaBase.esBase()) {
            throw new ReglaDominioException("La temporada inicial del alojamiento debe ser la temporada base");
        }
        Alojamiento alojamiento = new Alojamiento(UUID.randomUUID(), nombre.trim(), ciudad.trim(), parametros);
        alojamiento.temporadas.add(temporadaBase);
        alojamiento.historialPoliticas.add(PoliticaCancelacion.crear(1, vigenteDesde, tramosPoliticaInicial));
        return alojamiento;
    }

    /**
     * Reconstruye el Alojamiento ya persistido. Solo para rehidratar desde la persistencia: el estado guardado ya
     * pasó por crear(...) y por los métodos de negocio.
     */
    public static Alojamiento reconstruir(UUID id, String nombre, String ciudad, ParametrosAlojamiento parametros,
                                          List<Temporada> temporadas, List<PoliticaCancelacion> politicas) {
        Alojamiento alojamiento = new Alojamiento(id, nombre, ciudad, parametros);
        alojamiento.temporadas.addAll(temporadas);
        alojamiento.historialPoliticas.addAll(politicas);
        return alojamiento;
    }

    private static void validarIdentidad(String nombre, String ciudad) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El nombre del alojamiento no puede estar vacío");
        }
        if (ciudad == null || ciudad.isBlank()) {
            throw new ReglaDominioException("La ciudad del alojamiento no puede estar vacía");
        }
    }

    /** 7.4: las temporadas no pueden solaparse entre sí; solo puede existir una temporada base. */
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
     * 7.4: ninguna fecha reservable queda sin temporada. Se busca primero entre las temporadas específicas; si
     * ninguna cubre la fecha, se usa la base como respaldo.
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

    public Optional<Temporada> temporada(UUID idTemporada) {
        return temporadas.stream().filter(t -> t.id().equals(idTemporada)).findFirst();
    }

    /**
     * 7.4 / F-07: al actualizarla se conserva la versión anterior; nunca se edita una PoliticaCancelacion existente,
     * siempre se agrega una nueva. La nueva debe regir DESPUÉS de la más reciente: con la misma fecha habría dos
     * versiones "vigentes" a la vez y no se sabría cuál congelar en una reserva (RN-13).
     */
    public PoliticaCancelacion definirNuevaPolitica(List<TramoAntelacion> tramos, LocalDate vigenteDesde) {
        Objects.requireNonNull(vigenteDesde, "La fecha de vigencia no puede ser nula");
        boolean noEsPosterior = historialPoliticas.stream().anyMatch(p -> !vigenteDesde.isAfter(p.vigenteDesde()));
        if (noEsPosterior) {
            throw new ReglaDominioException("La nueva política debe regir después de la versión más reciente");
        }
        PoliticaCancelacion nueva = PoliticaCancelacion.crear(historialPoliticas.size() + 1, vigenteDesde, tramos);
        historialPoliticas.add(nueva);
        return nueva;
    }

    /** 3.5: la reserva congela la versión vigente al momento de crearse. */
    public PoliticaCancelacion politicaVigente(LocalDate fecha) {
        Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        return historialPoliticas.stream()
                .filter(p -> !p.vigenteDesde().isAfter(fecha))
                .max(Comparator.comparing(PoliticaCancelacion::vigenteDesde))
                .orElseThrow(() -> new ReglaDominioException("No hay una política de cancelación vigente para la fecha " + fecha));
    }

    /** 7.4: consulta una versión de la política por su número (1, 2, 3...). */
    public Optional<PoliticaCancelacion> politicaPorNumero(int numero) {
        return historialPoliticas.stream().filter(p -> p.numeroVersion().numero() == numero).findFirst();
    }

    /** RN-13: la retención se calcula con la versión congelada en la reserva, no con la vigente al momento del hecho. */
    public Optional<PoliticaCancelacion> politicaDeVersion(VersionPolitica version) {
        Objects.requireNonNull(version, "La versión no puede ser nula");
        return historialPoliticas.stream().filter(p -> p.numeroVersion().equals(version)).findFirst();
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

    public ParametrosAlojamiento parametros() {
        return parametros;
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

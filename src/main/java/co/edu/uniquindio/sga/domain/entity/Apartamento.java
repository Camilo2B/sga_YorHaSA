package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * F-01 - Unidad vendible. Raíz del agregado que compone Dormitorio, Bloqueo,
 * Tarifa y Novedad.
 * <p>
 * Reserva es una raíz de agregado DISTINTA: Apartamento nunca guarda una
 * lista de Reserva ni de Estancia reservada -eso viviría en otro agregado-.
 * Por eso estaDisponible(...) recibe las estancias activas como parámetro
 * (calculadas por quien sí tiene acceso al repositorio de Reserva) en vez
 * de mantenerlas internamente.
 * <p>
 * Identidad: "identificacion" (ej. "ATK-001") es la clave de negocio única y
 * estable que exige 7.3 -no se introduce un id sintético aparte, porque ya
 * existe un identificador natural del dominio-.
 */
public final class Apartamento {

    private final String identificacion;
    private final String nombre;
    private final int capacidad;
    private final List<Dormitorio> dormitorios;
    private EstadoOperativo estadoOperativo;
    private final List<Tarifa> tarifas = new ArrayList<>();
    private final List<Bloqueo> bloqueos = new ArrayList<>();
    private final List<Novedad> novedades = new ArrayList<>();
    private boolean activo;

    private Apartamento(String identificacion, String nombre, int capacidad,
                        List<Dormitorio> dormitorios, EstadoOperativo estadoOperativo) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.dormitorios = new ArrayList<>(dormitorios);
        this.estadoOperativo = estadoOperativo;
        this.activo = true;
    }

    public static Apartamento crear(String identificacion, String nombre, int capacidad,
                                    List<Dormitorio> dormitorios, EstadoOperativo estadoOperativoInicial) {
        if (identificacion == null || identificacion.isBlank()) {
            throw new ReglaDominioException("La identificación del apartamento no puede estar vacía");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El nombre del apartamento no puede estar vacío");
        }
        // F-03: la capacidad es un tope rígido, sin excepciones -por eso debe ser positiva desde el inicio-.
        if (capacidad <= 0) {
            throw new ReglaDominioException("La capacidad del apartamento debe ser mayor a cero");
        }
        Objects.requireNonNull(dormitorios, "La lista de dormitorios no puede ser nula");
        if (dormitorios.isEmpty()) {
            throw new ReglaDominioException("Un apartamento debe tener al menos un dormitorio");
        }
        Objects.requireNonNull(estadoOperativoInicial, "El estado operativo inicial no puede ser nulo");
        return new Apartamento(identificacion, nombre, capacidad, dormitorios, estadoOperativoInicial);
    }

    /**
     * 7.6: transición validada por el propio enum (ver EstadoOperativo);
     * aquí no se repite la tabla de transiciones, solo se delega en ella.
     * La restricción "FUERA_DE_SERVICIO solo por administrador" es una
     * regla de AUTORIZACIÓN (quién puede invocar esto), no de dominio: el
     * dominio no conoce roles, así que esa verificación vive en la capa de
     * aplicación, no aquí.
     */
    public void cambiarEstadoOperativo(EstadoOperativo nuevo) {
        Objects.requireNonNull(nuevo, "El nuevo estado operativo no puede ser nulo");
        if (!this.estadoOperativo.puedeTransitarA(nuevo)) {
            throw new ReglaDominioException(
                    "Transición de estado operativo inválida: " + this.estadoOperativo + " -> " + nuevo);
        }
        this.estadoOperativo = nuevo;
    }

    /**
     * RN-01, RN-02, RN-07 combinadas: un apartamento está disponible para
     * una estancia si está activo, su capacidad alcanza para el grupo, y la
     * estancia no se solapa con ningún bloqueo propio ni con ninguna
     * reserva activa de otros agregados.
     * <p>
     * Las reservas activas se reciben como parámetro (no se guardan aquí)
     * porque pertenecen al agregado Reserva; quien llama a este método ya
     * las obtuvo de ReservaRepository.
     */
    public boolean estaDisponible(Estancia estanciaSolicitada, int totalOcupantes, List<Estancia> estanciasReservasActivas) {
        Objects.requireNonNull(estanciaSolicitada, "La estancia solicitada no puede ser nula");
        Objects.requireNonNull(estanciasReservasActivas, "La lista de estancias reservadas no puede ser nula");

        if (!activo) {
            return false;
        }
        if (totalOcupantes > capacidad) {
            return false;
        }
        boolean bloqueado = bloqueos.stream().anyMatch(b -> b.seSolapaCon(estanciaSolicitada));
        if (bloqueado) {
            return false;
        }
        return estanciasReservasActivas.stream().noneMatch(e -> e.seSolapaCon(estanciaSolicitada));
    }

    /**
     * Autocontenido: valida solo contra los bloqueos ya existentes de este
     * apartamento. La regla completa (7.3: "no puede registrarse un bloqueo
     * sobre noches que ya tengan reservas activas") necesita consultar el
     * agregado Reserva -eso es trabajo de un servicio de dominio, no de
     * este método-.
     */
    public void agregarBloqueo(Bloqueo nuevo) {
        Objects.requireNonNull(nuevo, "El bloqueo no puede ser nulo");
        boolean solapaConOtroBloqueo = bloqueos.stream().anyMatch(b -> b.seSolapaCon(nuevo.rango()));
        if (solapaConOtroBloqueo) {
            throw new ReglaDominioException("El nuevo bloqueo se solapa con un bloqueo existente de este apartamento");
        }
        bloqueos.add(nuevo);
    }

    /**
     * 7.4: toda combinación apartamento × temporada debe tener tarifa. No
     * se valida aquí la unicidad de (temporadaId, vigenteDesde): si se
     * agregan dos tarifas con la misma vigencia para la misma temporada,
     * tarifaVigente(...) simplemente toma la más reciente por orden de
     * inserción entre las empatadas -se documenta como supuesto abierto-.
     */
    public void agregarTarifa(Tarifa nueva) {
        Objects.requireNonNull(nueva, "La tarifa no puede ser nula");
        tarifas.add(nueva);
    }

    public Tarifa tarifaVigente(java.util.UUID temporadaId, LocalDate fecha) {
        Objects.requireNonNull(temporadaId, "El identificador de temporada no puede ser nulo");
        Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        return tarifas.stream()
                .filter(t -> t.temporadaId().equals(temporadaId))
                .filter(t -> !t.vigenteDesde().isAfter(fecha))
                .max(Comparator.comparing(Tarifa::vigenteDesde))
                .orElseThrow(() -> new ReglaDominioException(
                        "El apartamento " + identificacion + " no tiene tarifa vigente para la temporada indicada en la fecha " + fecha));
    }

    public void registrarNovedad(Novedad novedad) {
        Objects.requireNonNull(novedad, "La novedad no puede ser nula");
        novedades.add(novedad);
    }

    /**
     * 7.3: un apartamento puede retirarse de la venta solo si no tiene
     * reservas activas ni futuras. Esa verificación necesita el agregado
     * Reserva, así que quien invoque este método (un servicio de dominio)
     * debe haberla hecho antes de llamarlo -Apartamento confía en su
     * llamador para esta precondición, igual que con agregarBloqueo-.
     */
    public void desactivar() {
        this.activo = false;
    }

    public String identificacion() {
        return identificacion;
    }

    public String nombre() {
        return nombre;
    }

    public int capacidad() {
        return capacidad;
    }

    public List<Dormitorio> dormitorios() {
        return List.copyOf(dormitorios);
    }

    public EstadoOperativo estadoOperativo() {
        return estadoOperativo;
    }

    public List<Tarifa> tarifas() {
        return List.copyOf(tarifas);
    }

    public List<Bloqueo> bloqueos() {
        return List.copyOf(bloqueos);
    }

    public List<Novedad> novedades() {
        return List.copyOf(novedades);
    }

    public boolean activo() {
        return activo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Apartamento otro)) return false;
        return this.identificacion.equals(otro.identificacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identificacion);
    }
}
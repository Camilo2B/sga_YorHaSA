package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * F-07 - Existe una única política de cancelación vigente por alojamiento,
 * versionada en el tiempo (7.4).
 * <p>
 * Es Entidad -y no Value Object- porque necesita identidad propia por
 * versión: RN-13 exige que la retención de una reserva se calcule con la
 * versión de política que estaba congelada en ella, no con la vigente al
 * momento del hecho. Eso solo es posible si cada versión es distinguible.
 * <p>
 * Inmutable por diseño (sin setters): "actualizar" la política nunca edita
 * esta instancia, siempre crea una versión nueva
 * (ver Alojamiento.definirNuevaPolitica).
 */
public final class PoliticaCancelacion {

    private static final int MINIMO_TRAMOS = 2;

    private final VersionPolitica numeroVersion;
    private final LocalDate vigenteDesde;
    private final List<TramoAntelacion> tramos;

    private PoliticaCancelacion(VersionPolitica numeroVersion, LocalDate vigenteDesde, List<TramoAntelacion> tramos) {
        this.numeroVersion = numeroVersion;
        this.vigenteDesde = vigenteDesde;
        this.tramos = tramos;
    }

    public static PoliticaCancelacion crear(int numeroVersion, LocalDate vigenteDesde, List<TramoAntelacion> tramos) {
        if (numeroVersion < 1) {
            throw new ReglaDominioException("El número de versión debe ser positivo");
        }
        Objects.requireNonNull(vigenteDesde, "La fecha de vigencia no puede ser nula");
        Objects.requireNonNull(tramos, "Los tramos de antelación no pueden ser nulos");
        // 7.4: mínimo dos tramos de antelación y su retención.
        if (tramos.size() < MINIMO_TRAMOS) {
            throw new ReglaDominioException("Una política de cancelación debe tener al menos " + MINIMO_TRAMOS + " tramos de antelación");
        }
        // Sin repetir antelaciones: con dos tramos iguales no se sabría cuál retención aplicar.
        long antelacionesDistintas = tramos.stream().map(TramoAntelacion::antelacionMinimaHoras).distinct().count();
        if (antelacionesDistintas != tramos.size()) {
            throw new ReglaDominioException("Los tramos de la política no pueden repetir la misma antelación mínima");
        }
        // RN-13: toda cancelación debe caer en algún tramo; el de 0 horas es el que cubre las más cercanas a la entrada.
        if (tramos.stream().noneMatch(t -> t.antelacionMinimaHoras() == 0)) {
            throw new ReglaDominioException("La política debe tener un tramo de 0 horas de antelación para cubrir las cancelaciones más cercanas a la entrada");
        }
        return new PoliticaCancelacion(new VersionPolitica(numeroVersion, vigenteDesde), vigenteDesde, List.copyOf(tramos));
    }

    /** Reconstruye una política ya persistida. Solo para rehidratar desde la persistencia. */
    public static PoliticaCancelacion reconstruir(VersionPolitica version, List<TramoAntelacion> tramos) {
        return new PoliticaCancelacion(version, version.vigenteDesde(), List.copyOf(tramos));
    }

    /**
     * RN-13: retención aplicable según la antelación real de la cancelación.
     * Se elige el tramo con la mayor antelación mínima que la antelación
     * real todavía satisface (el tramo más específico que aplica).
     */
    public BigDecimal retencionAplicable(long horasDeAntelacion) {
        return tramos.stream()
                .filter(t -> t.aplicaPara(horasDeAntelacion))
                .max(Comparator.comparingInt(TramoAntelacion::antelacionMinimaHoras))
                .map(TramoAntelacion::porcentajeRetencion)
                .orElseThrow(() -> new ReglaDominioException(
                        "Ningún tramo de la política de versión " + numeroVersion + " cubre " + horasDeAntelacion + " horas de antelación"));
    }

    public VersionPolitica numeroVersion() {
        return numeroVersion;
    }

    public LocalDate vigenteDesde() {
        return vigenteDesde;
    }

    public List<TramoAntelacion> tramos() {
        return tramos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PoliticaCancelacion otra)) return false;
        return this.numeroVersion.equals(otra.numeroVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroVersion);
    }
}
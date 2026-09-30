package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion;

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

    private final int numeroVersion;
    private final LocalDate vigenteDesde;
    private final List<TramoAntelacion> tramos;

    private PoliticaCancelacion(int numeroVersion, LocalDate vigenteDesde, List<TramoAntelacion> tramos) {
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
        return new PoliticaCancelacion(numeroVersion, vigenteDesde, List.copyOf(tramos));
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

    public int numeroVersion() {
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
        return this.numeroVersion == otra.numeroVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroVersion);
    }
}
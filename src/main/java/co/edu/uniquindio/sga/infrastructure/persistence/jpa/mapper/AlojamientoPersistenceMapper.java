package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.TramoAntelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.AlojamientoEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.PoliticaCancelacionEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TemporadaEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TramoEmbeddable;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Traduce persistencia <-> dominio. Al volver al dominio recibe los parámetros ya armados (vienen de la
 * configuración, no de la base) y usa las fábricas reconstruir(...), así el dominio no necesita setters.
 */
@Component
public class AlojamientoPersistenceMapper {

    public AlojamientoEntity toEntity(Alojamiento alojamiento) {
        AlojamientoEntity entity = new AlojamientoEntity(alojamiento.id(), alojamiento.nombre(), alojamiento.ciudad());
        alojamiento.temporadas().forEach(t -> entity.agregarTemporada(
                new TemporadaEntity(t.id(), t.nombre(), t.fechaInicio(), t.fechaFin(), t.esBase())));
        alojamiento.historialPoliticas().forEach(p -> entity.agregarPolitica(new PoliticaCancelacionEntity(
                idDePolitica(alojamiento.id(), p.numeroVersion().numero()),
                p.numeroVersion().numero(),
                p.vigenteDesde(),
                p.tramos().stream()
                        .map(t -> new TramoEmbeddable(t.antelacionMinimaHoras(), t.porcentajeRetencion()))
                        .toList())));
        return entity;
    }

    public Alojamiento toDomain(AlojamientoEntity entity, ParametrosAlojamiento parametros) {
        List<Temporada> temporadas = entity.getTemporadas().stream()
                .map(t -> Temporada.reconstruir(t.getId(), t.getNombre(), t.getFechaInicio(), t.getFechaFin(), t.isEsBase()))
                .toList();
        List<PoliticaCancelacion> politicas = entity.getPoliticas().stream()
                .sorted(Comparator.comparingInt(PoliticaCancelacionEntity::getNumeroVersion))
                .map(p -> PoliticaCancelacion.reconstruir(
                        new VersionPolitica(p.getNumeroVersion(), p.getVigenteDesde()),
                        p.getTramos().stream()
                                .map(t -> new TramoAntelacion(t.antelacionMinimaHoras(), t.porcentajeRetencion()))
                                .toList()))
                .toList();
        return Alojamiento.reconstruir(entity.getId(), entity.getNombre(), entity.getCiudad(), parametros, temporadas, politicas);
    }

    /**
     * El dominio identifica cada política por su versión, no por un UUID. Para que guardar varias veces no recree
     * filas, el id de la fila se deriva siempre igual a partir de (alojamiento, versión).
     */
    private static UUID idDePolitica(UUID alojamientoId, int numeroVersion) {
        return UUID.nameUUIDFromBytes(("politica:" + alojamientoId + ":" + numeroVersion).getBytes(StandardCharsets.UTF_8));
    }
}

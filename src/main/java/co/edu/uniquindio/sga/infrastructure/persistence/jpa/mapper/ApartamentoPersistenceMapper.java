package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.TipoApartamento;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ApartamentoEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.BloqueoEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.DormitorioEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.EstanciaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.NovedadEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TarifaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Traduce persistencia <-> dominio. Para volver al dominio usa las fábricas reconstruir(...), de modo que
 * el dominio no necesita setters, constructor vacío ni anotaciones de JPA. Es manual (sin MapStruct) porque
 * reconstruir objetos con identidad y fábricas estáticas es más claro y seguro escrito a mano.
 */
@Component
public class ApartamentoPersistenceMapper {

    public ApartamentoEntity toEntity(Apartamento apartamento) {
        TipoApartamento tipo = apartamento.tipo();
        ApartamentoEntity entity = new ApartamentoEntity(
                apartamento.identificacion(),
                apartamento.nombre(),
                apartamento.capacidad(),
                tipo == null ? null : tipo.nombreTipo(),
                tipo == null ? List.of() : tipo.caracteristicas(),
                apartamento.estadoOperativo(),
                apartamento.activo());

        apartamento.dormitorios().forEach(d ->
                entity.getDormitorios().add(new DormitorioEmbeddable(d.descripcion(), d.tipoCama())));
        apartamento.tarifas().forEach(t -> entity.agregarTarifa(
                new TarifaEntity(t.id(), t.temporadaId(), t.valorPorNoche().monto(), t.vigenteDesde())));
        apartamento.bloqueos().forEach(b -> entity.agregarBloqueo(
                new BloqueoEntity(b.id(), new EstanciaEmbeddable(b.rango().entrada(), b.rango().salida()), b.motivo())));
        apartamento.novedades().forEach(n -> entity.agregarNovedad(
                new NovedadEntity(n.id(), n.fecha(), n.autor(), n.descripcion(), n.gravedad())));
        return entity;
    }

    public Apartamento toDomain(ApartamentoEntity entity) {
        TipoApartamento tipo = entity.getTipoNombre() == null
                ? null
                : new TipoApartamento(entity.getTipoNombre(), entity.getTipoCaracteristicas());

        List<Dormitorio> dormitorios = entity.getDormitorios().stream()
                .map(d -> new Dormitorio(d.descripcion(), d.tipoCama()))
                .toList();
        List<Tarifa> tarifas = entity.getTarifas().stream()
                .map(t -> Tarifa.reconstruir(t.getId(), t.getTemporadaId(), new Dinero(t.getValorPorNoche()), t.getVigenteDesde()))
                .toList();
        List<Bloqueo> bloqueos = entity.getBloqueos().stream()
                .map(b -> Bloqueo.reconstruir(b.getId(), new Estancia(b.getRango().entrada(), b.getRango().salida()), b.getMotivo()))
                .toList();
        List<Novedad> novedades = entity.getNovedades().stream()
                .map(n -> Novedad.reconstruir(n.getId(), n.getFecha(), n.getAutor(), n.getDescripcion(), n.getGravedad()))
                .toList();

        return Apartamento.reconstruir(entity.getIdentificacion(), entity.getNombre(), entity.getCapacidad(),
                dormitorios, tipo, entity.getEstadoOperativo(), tarifas, bloqueos, novedades, entity.isActivo());
    }
}

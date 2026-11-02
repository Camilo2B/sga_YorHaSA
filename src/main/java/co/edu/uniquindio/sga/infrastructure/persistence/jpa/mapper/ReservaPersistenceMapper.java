package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Cargo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Titular;
import co.edu.uniquindio.sga.domain.valueobject.Cancelacion;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DetalleNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CargoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.DetalleNocheEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.EstanciaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.NovedadReservaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.OcupanteEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.PagoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Traduce entre el agregado Reserva (dominio puro) y su entidad JPA. Toda la reconstrucción pasa por fábricas del
 * dominio (reconstruir / crear), así la persistencia no obliga a agregar setters ni constructores vacíos al dominio.
 */
@Component
public class ReservaPersistenceMapper {

    public ReservaEntity toEntity(Reserva reserva) {
        ReservaEntity entity = new ReservaEntity(
                reserva.getId(),
                reserva.getCodigo().valor(),
                reserva.getApartamentoId(),
                toEmbeddable(reserva.getTitular()),
                new EstanciaEmbeddable(reserva.getEstancia().entrada(), reserva.getEstancia().salida()),
                reserva.getCanalOrigen(),
                reserva.getEstado(),
                reserva.getVersionPolitica().numero(),
                reserva.getVersionPolitica().vigenteDesde(),
                reserva.getFechaCreacion(),
                reserva.isConMascota(),
                reserva.getHoraEstimadaLlegada(),
                reserva.getFolio().getId());

        reserva.getCancelacion().ifPresent(c -> entity.asignarCancelacion(
                c.motivo(), c.fecha(), c.retenido().monto(), c.devuelto().monto()));
        reserva.getCotizacion().desglosePorNoche().forEach(d -> entity.getDetalleNoches().add(
                new DetalleNocheEmbeddable(d.fecha(), d.nombreTemporada(), d.tarifaAplicada().monto(), d.ocupantesFacturables())));
        reserva.getOcupantes().forEach(o -> entity.getOcupantes().add(toEmbeddable(o)));
        reserva.getFolio().getCargos().forEach(c -> entity.getCargos().add(
                new CargoEmbeddable(c.id(), c.tipo(), c.valor().monto())));
        reserva.getFolio().getPagos().forEach(p -> entity.getPagos().add(
                new PagoEmbeddable(p.id(), p.medio(), p.fecha(), p.valor().monto())));
        reserva.getNovedades().forEach(n -> entity.getNovedades().add(
                new NovedadReservaEmbeddable(n.id(), n.fecha(), n.autor(), n.descripcion(), n.gravedad())));
        return entity;
    }

    public Reserva toDomain(ReservaEntity entity) {
        Titular titular = Titular.crear(entity.getTitular().documento(), entity.getTitular().nombre(),
                entity.getTitular().fechaNacimiento());
        Estancia estancia = new Estancia(entity.getEstancia().entrada(), entity.getEstancia().salida());

        List<DetalleNoche> desglose = entity.getDetalleNoches().stream()
                .map(d -> new DetalleNoche(d.fecha(), d.nombreTemporada(), new Dinero(d.tarifaAplicada()), d.ocupantesFacturables()))
                .toList();
        List<Ocupante> ocupantes = entity.getOcupantes().stream()
                .map(o -> Ocupante.crear(o.documento(), o.nombre(), o.fechaNacimiento()))
                .toList();
        List<Cargo> cargos = entity.getCargos().stream()
                .map(c -> Cargo.reconstruir(c.id(), c.tipo(), new Dinero(c.valor())))
                .toList();
        List<Pago> pagos = entity.getPagos().stream()
                .map(p -> Pago.reconstruir(p.id(), p.medio(), p.fecha(), new Dinero(p.valor())))
                .toList();
        List<Novedad> novedades = entity.getNovedades().stream()
                .map(n -> Novedad.reconstruir(n.id(), n.fecha(), n.autor(), n.descripcion(), n.gravedad()))
                .toList();

        Cancelacion cancelacion = entity.getMotivoCancelacion() == null
                ? null
                : new Cancelacion(entity.getMotivoCancelacion(), entity.getFechaCancelacion(),
                        new Dinero(entity.getMontoRetenido()), new Dinero(entity.getMontoDevuelto()));

        return Reserva.reconstruir(
                entity.getId(),
                new CodigoReserva(entity.getCodigo()),
                entity.getApartamentoId(),
                titular,
                estancia,
                entity.getCanalOrigen(),
                entity.getEstado(),
                new Cotizacion(desglose),
                new VersionPolitica(entity.getVersionPoliticaNumero(), entity.getVersionPoliticaVigenteDesde()),
                entity.getFechaCreacion(),
                entity.isConMascota(),
                entity.getHoraEstimadaLlegada(),
                cancelacion,
                Folio.reconstruir(entity.getFolioId(), entity.getId(), cargos, pagos),
                ocupantes,
                novedades);
    }

    private static OcupanteEmbeddable toEmbeddable(Ocupante ocupante) {
        return new OcupanteEmbeddable(ocupante.documento(), ocupante.nombre(), ocupante.fechaNacimiento());
    }
}

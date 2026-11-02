package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.request.ConfirmarReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.OcupanteRequest;
import co.edu.uniquindio.sga.application.dto.response.OcupanteResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaDetalleResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResumenResponse;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Titular;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Cancelacion;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.SolicitudReserva;
import org.mapstruct.Mapper;

import java.time.LocalTime;
import java.util.List;

/**
 * DTO <-> dominio sin lógica de negocio. La entrada pasa por los constructores y fábricas del dominio (Estancia,
 * Titular.crear, Ocupante.crear): el request no puede saltarse sus validaciones. Se escribe con métodos por defecto
 * porque Titular es un Ocupante y MapStruct no sabría cuál de los dos métodos usar para una lista.
 */
@Mapper(componentModel = "spring")
public interface ReservaMapper {

    default SolicitudReserva toSolicitud(CrearReservaRequest request) {
        return new SolicitudReserva(
                toTitular(request.titular()),
                toAcompanantes(request.ocupantes()),
                new Estancia(request.fechaEntrada(), request.fechaSalida()),
                request.canalOrigen(),
                Boolean.TRUE.equals(request.conMascota()),
                request.horaEstimadaLlegada());
    }

    default Titular toTitular(OcupanteRequest request) {
        return Titular.crear(request.documento(), request.nombre(), request.fechaNacimiento());
    }

    default List<Ocupante> toAcompanantes(List<OcupanteRequest> requests) {
        return requests.stream()
                .map(r -> Ocupante.crear(r.documento(), r.nombre(), r.fechaNacimiento()))
                .toList();
    }

    /** Un código con formato inválido tampoco corresponde a ninguna reserva: se trata como no encontrada (404). */
    default CodigoReserva toCodigo(String valor) {
        try {
            return new CodigoReserva(valor);
        } catch (ReglaDominioException e) {
            throw new ReservaNoEncontradaException(valor);
        }
    }

    /** El cuerpo de confirmar es opcional: sin cuerpo no hay medio de pago. */
    default MedioPago toMedioPago(ConfirmarReservaRequest request) {
        return request == null ? null : request.medioPago();
    }

    default LocalTime toHoraEstimadaLlegada(ConfirmarReservaRequest request) {
        return request == null ? null : request.horaEstimadaLlegada();
    }

    default String toReferencia(ConfirmarReservaRequest request) {
        return request == null ? null : request.referencia();
    }

    default OcupanteResponse toOcupanteResponse(Ocupante ocupante) {
        return new OcupanteResponse(ocupante.documento(), ocupante.nombre(), ocupante.fechaNacimiento());
    }

    default ReservaResumenResponse toResumenResponse(Reserva reserva) {
        return new ReservaResumenResponse(
                reserva.getCodigo().valor(),
                reserva.getEstado(),
                reserva.getApartamentoId(),
                reserva.getEstancia().entrada(),
                reserva.getEstancia().salida(),
                reserva.getTitular().nombre(),
                reserva.getOcupantes().size(),
                reserva.getCotizacion().valorTotal().monto().longValueExact());
    }

    default ReservaDetalleResponse toDetalleResponse(Reserva reserva) {
        return new ReservaDetalleResponse(
                reserva.getCodigo().valor(),
                reserva.getEstado(),
                reserva.getCanalOrigen(),
                reserva.getApartamentoId(),
                reserva.getEstancia().entrada(),
                reserva.getEstancia().salida(),
                (int) reserva.getEstancia().noches(),
                reserva.getHoraEstimadaLlegada(),
                reserva.isConMascota(),
                toOcupanteResponse(reserva.getTitular()),
                reserva.getOcupantes().stream().map(this::toOcupanteResponse).toList(),
                reserva.getOcupantes().size(),
                reserva.getCotizacion().valorTotal().monto().longValueExact(),
                reserva.getFolio().calcularTotalCargos().monto().longValueExact(),
                reserva.getCotizacion().valorTotal().moneda(),
                "v" + reserva.getVersionPolitica().numero(),
                reserva.getFechaCreacion().toLocalDate(),
                reserva.getCancelacion().map(Cancelacion::motivo).orElse(null),
                reserva.getCancelacion().map(c -> c.retenido().monto().longValueExact()).orElse(null),
                reserva.getCancelacion().map(c -> c.devuelto().monto().longValueExact()).orElse(null));
    }
}

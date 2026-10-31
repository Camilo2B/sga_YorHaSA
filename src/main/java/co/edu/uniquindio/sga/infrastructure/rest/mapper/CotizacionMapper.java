package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.request.CotizarEstanciaRequest;
import co.edu.uniquindio.sga.application.dto.request.OcupanteRequest;
import co.edu.uniquindio.sga.application.dto.response.CotizacionResponse;
import co.edu.uniquindio.sga.application.dto.response.DetalleNocheResponse;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DetalleNoche;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * DTO <-> dominio sin lógica de negocio. La entrada pasa por Estancia y Ocupante.crear(...): el request no puede
 * saltarse las validaciones del dominio.
 */
@Mapper(componentModel = "spring")
public interface CotizacionMapper {

    default Estancia toEstancia(CotizarEstanciaRequest request) {
        return new Estancia(request.fechaEntrada(), request.fechaSalida());
    }

    default Ocupante toOcupante(OcupanteRequest request) {
        return Ocupante.crear(request.documento(), request.nombre(), request.fechaNacimiento());
    }

    List<Ocupante> toOcupantes(List<OcupanteRequest> requests);

    default DetalleNocheResponse toDetalleResponse(DetalleNoche detalle) {
        return new DetalleNocheResponse(detalle.fecha(), detalle.nombreTemporada(),
                detalle.tarifaAplicada().monto().longValueExact(), detalle.ocupantesFacturables(),
                detalle.subtotal().monto().longValueExact());
    }

    List<DetalleNocheResponse> toDetalleResponses(List<DetalleNoche> detalles);

    default CotizacionResponse toResponse(String identificacionApartamento, Estancia estancia, Cotizacion cotizacion) {
        return new CotizacionResponse(
                identificacionApartamento,
                estancia.entrada(),
                estancia.salida(),
                cotizacion.totalNoches(),
                cotizacion.desglosePorNoche().get(0).ocupantesFacturables(),
                toDetalleResponses(cotizacion.desglosePorNoche()),
                cotizacion.valorTotal().monto().longValueExact(),
                cotizacion.valorTotal().moneda());
    }
}

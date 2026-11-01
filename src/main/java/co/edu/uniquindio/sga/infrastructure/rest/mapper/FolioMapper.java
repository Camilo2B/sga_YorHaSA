package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.response.CargoResponse;
import co.edu.uniquindio.sga.application.dto.response.FolioResponse;
import co.edu.uniquindio.sga.application.dto.response.PagoResponse;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Cargo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

/** DTO <-> dominio del folio, sin lógica de negocio. */
@Mapper(componentModel = "spring")
public interface FolioMapper {

    default CodigoReserva toCodigo(String valor) {
        try {
            return new CodigoReserva(valor);
        } catch (ReglaDominioException e) {
            throw new ReservaNoEncontradaException(valor);
        }
    }

    default Dinero toDinero(BigDecimal valor) {
        return Dinero.de(valor);
    }

    default CargoResponse toCargoResponse(Cargo cargo) {
        return new CargoResponse(cargo.id(), cargo.tipo(), cargo.valor().monto().longValueExact());
    }

    default PagoResponse toPagoResponse(Pago pago) {
        return new PagoResponse(pago.id(), pago.medio(), pago.fecha(), pago.valor().monto().longValueExact());
    }

    default FolioResponse toResponse(String codigoReserva, Folio folio) {
        return new FolioResponse(
                codigoReserva,
                folio.getCargos().stream().map(this::toCargoResponse).toList(),
                folio.getPagos().stream().map(this::toPagoResponse).toList(),
                folio.calcularTotalCargos().monto().longValueExact(),
                folio.calcularTotalPagos().monto().longValueExact(),
                folio.calcularSaldoPendiente().monto().longValueExact(),
                folio.calcularTotalCargos().moneda());
    }
}

package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.request.BloquearApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.request.BuscarDisponibilidadRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.request.DormitorioRequest;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoResponse;
import co.edu.uniquindio.sga.application.dto.response.BloqueoResponse;
import co.edu.uniquindio.sga.application.dto.response.DormitorioResponse;
import co.edu.uniquindio.sga.application.dto.response.TarifaResponse;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO <-> dominio sin lógica de negocio. El dominio expone accesores "fluidos" (identificacion(), no getIdentificacion()),
 * que MapStruct no reconoce como propiedades, por eso los campos de Apartamento se indican con expression.
 * La creación pasa por Apartamento.crear(...), así que el request no puede saltarse las validaciones del dominio.
 */
@Mapper(componentModel = "spring")
public interface ApartamentoMapper {

    default Apartamento toDomain(CrearApartamentoRequest request) {
        return Apartamento.crear(
                request.identificacion(),
                request.nombre(),
                request.capacidad(),
                toDormitorios(request.dormitorios()),
                null,
                Boolean.TRUE.equals(request.permiteMascotas()),
                request.estadoOperativoInicial());
    }

    /** RN-07: el rango del bloqueo es semiabierto [fechaInicio, fechaFin), igual que una estancia. */
    default Bloqueo toBloqueo(BloquearApartamentoRequest request) {
        return Bloqueo.crear(new Estancia(request.fechaInicio(), request.fechaFin()), request.motivo());
    }

    default Estancia toEstancia(BuscarDisponibilidadRequest request) {
        return new Estancia(request.fechaEntrada(), request.fechaSalida());
    }

    /** Todos los ocupantes (adultos y niños) cuentan para la capacidad. Sin niños indicados se asume 0. */
    default int toTotalOcupantes(BuscarDisponibilidadRequest request) {
        return request.adultos() + (request.ninos() == null ? 0 : request.ninos());
    }

    default boolean toConMascota(BuscarDisponibilidadRequest request) {
        return Boolean.TRUE.equals(request.conMascota());
    }

    default Dinero toDinero(BigDecimal valor) {
        return Dinero.de(valor);
    }

    List<Dormitorio> toDormitorios(List<DormitorioRequest> requests);

    Dormitorio toDormitorio(DormitorioRequest request);

    @Mapping(target = "identificacion", expression = "java(apartamento.identificacion())")
    @Mapping(target = "nombre", expression = "java(apartamento.nombre())")
    @Mapping(target = "capacidad", expression = "java(apartamento.capacidad())")
    @Mapping(target = "dormitorios", expression = "java(toDormitorioResponses(apartamento.dormitorios()))")
    @Mapping(target = "permiteMascotas", expression = "java(apartamento.permiteMascotas())")
    @Mapping(target = "estadoOperativo", expression = "java(apartamento.estadoOperativo())")
    @Mapping(target = "activo", expression = "java(apartamento.activo())")
    ApartamentoResponse toResponse(Apartamento apartamento);

    List<DormitorioResponse> toDormitorioResponses(List<Dormitorio> dormitorios);

    DormitorioResponse toDormitorioResponse(Dormitorio dormitorio);

    default TarifaResponse toTarifaResponse(Tarifa tarifa) {
        return new TarifaResponse(tarifa.id(), tarifa.temporadaId(), tarifa.valorPorNoche().monto(), tarifa.vigenteDesde());
    }

    List<TarifaResponse> toTarifaResponses(List<Tarifa> tarifas);

    default BloqueoResponse toBloqueoResponse(String identificacionApartamento, Bloqueo bloqueo) {
        return new BloqueoResponse(bloqueo.id(), identificacionApartamento, bloqueo.rango().entrada(),
                bloqueo.rango().salida(), bloqueo.motivo());
    }
}

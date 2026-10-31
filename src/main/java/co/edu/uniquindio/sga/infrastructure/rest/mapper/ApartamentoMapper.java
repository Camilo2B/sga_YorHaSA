package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.request.CrearApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.request.DormitorioRequest;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoResponse;
import co.edu.uniquindio.sga.application.dto.response.DormitorioResponse;
import co.edu.uniquindio.sga.application.dto.response.TarifaResponse;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
                request.estadoOperativoInicial());
    }

    List<Dormitorio> toDormitorios(List<DormitorioRequest> requests);

    Dormitorio toDormitorio(DormitorioRequest request);

    @Mapping(target = "identificacion", expression = "java(apartamento.identificacion())")
    @Mapping(target = "nombre", expression = "java(apartamento.nombre())")
    @Mapping(target = "capacidad", expression = "java(apartamento.capacidad())")
    @Mapping(target = "dormitorios", expression = "java(toDormitorioResponses(apartamento.dormitorios()))")
    @Mapping(target = "estadoOperativo", expression = "java(apartamento.estadoOperativo())")
    @Mapping(target = "activo", expression = "java(apartamento.activo())")
    ApartamentoResponse toResponse(Apartamento apartamento);

    List<DormitorioResponse> toDormitorioResponses(List<Dormitorio> dormitorios);

    DormitorioResponse toDormitorioResponse(Dormitorio dormitorio);

    default TarifaResponse toTarifaResponse(Tarifa tarifa) {
        return new TarifaResponse(tarifa.id(), tarifa.temporadaId(), tarifa.valorPorNoche().monto(), tarifa.vigenteDesde());
    }

    List<TarifaResponse> toTarifaResponses(List<Tarifa> tarifas);
}

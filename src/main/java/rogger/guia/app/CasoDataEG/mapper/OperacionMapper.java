package rogger.guia.app.CasoDataEG.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rogger.guia.app.CasoDataEG.model.dto.response.OperacionResponse;
import rogger.guia.app.CasoDataEG.model.entity.Operacion;

@Mapper(componentModel = "spring")
public interface OperacionMapper {

    @Mapping(target = "idoperacion", source = "idOperacion")
    @Mapping(target = "fechaoperacion", source = "fechaOperacion")
    @Mapping(target = "obtieneextranjero", source = "obtieneOperacion")
    @Mapping(target = "valorventa", source = "valorVenta")
    @Mapping(target = "valorcompra", source = "valorCompra")
    @Mapping(target = "ventalocal", source = "ventaOperacion")
    @Mapping(target = "idmonedacambio", source = "idmonedatipocambio")
    @Mapping(target = "idmonedaorigen", source = "idmonedatipoorigen")
    @Mapping(target = "nombrepersona", source = "cliente.nombreCliente")
    OperacionResponse entityToResponse(Operacion operacion);

}

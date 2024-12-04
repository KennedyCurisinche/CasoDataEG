package rogger.guia.app.CasoDataEG.service.impl;

import org.springframework.stereotype.Component;
import rogger.guia.app.CasoDataEG.client.TipoCambioClient;
import rogger.guia.app.CasoDataEG.mapper.OperacionMapper;
import rogger.guia.app.CasoDataEG.model.dto.request.OperacionRequest;
import rogger.guia.app.CasoDataEG.model.dto.response.OperacionResponse;
import rogger.guia.app.CasoDataEG.model.dto.response.TipoCambioResponse;
import rogger.guia.app.CasoDataEG.model.entity.Cliente;
import rogger.guia.app.CasoDataEG.model.entity.Operacion;
import rogger.guia.app.CasoDataEG.model.entity.Saldo;
import rogger.guia.app.CasoDataEG.model.entity.TipoCambio;
import rogger.guia.app.CasoDataEG.repository.ClienteRepository;
import rogger.guia.app.CasoDataEG.repository.OperacionRepository;
import rogger.guia.app.CasoDataEG.repository.SaldoRepository;
import rogger.guia.app.CasoDataEG.repository.TipoCambioRepository;
import rogger.guia.app.CasoDataEG.service.OperacionService;
import rogger.guia.app.CasoDataEG.util.ClockPe;
import rogger.guia.app.CasoDataEG.util.ConstMoney;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class OperacionServiceImpl implements OperacionService {

    private final OperacionRepository operacionRepository;
    private final ClienteRepository clienteRepository;
    private final SaldoRepository saldoRepository;
    private final TipoCambioClient tipoCambioClient;
    private final TipoCambioRepository tipoCambioRepository;
    private final OperacionMapper operacionMapper;

    public OperacionServiceImpl(OperacionRepository operacionRepository, ClienteRepository clienteRepository, SaldoRepository saldoRepository, TipoCambioClient tipoCambioClient, TipoCambioRepository tipoCambioRepository, OperacionMapper operacionMapper) {
        this.operacionRepository = operacionRepository;
        this.clienteRepository = clienteRepository;
        this.saldoRepository = saldoRepository;
        this.tipoCambioClient = tipoCambioClient;
        this.tipoCambioRepository = tipoCambioRepository;
        this.operacionMapper = operacionMapper;
    }

    @Override
    public List<OperacionResponse> listaOperacion() {
        return operacionRepository.findAll().stream().map(operacionMapper::entityToResponse).collect(Collectors.toList());
    }

    @Override
    public void realizarCambio(OperacionRequest operacionRequest) {

        TipoCambioResponse tipoCambioResponse = tipoCambioClient.getTextTipoCambio();
        TipoCambio tipoCambio = tipoCambioRepository.findById(operacionRequest.getIdtipocambio()).orElse(null);

        if (Objects.equals(tipoCambio.getIdTipoCambio(), ConstMoney.MONEY_USD) && Objects.equals(operacionRequest.getIdtipoorigen(), ConstMoney.MONEY_SOLES) && Objects.nonNull(tipoCambio)) {

            Cliente cliente = clienteRepository.findById(operacionRequest.getIdcliente()).orElse(null);

            BigDecimal valorRestaLocal = operacionRequest.getMonto().multiply(tipoCambioResponse.getCompra());

            saldoRepository.saveAndFlush(
                    Saldo.builder()
                            .idSaldo(cliente.getSaldo().getIdSaldo())
                            .disponibleLocalSaldo(cliente.getSaldo().getDisponibleLocalSaldo().subtract(valorRestaLocal))
                            .disponibleExtranjeraSaldo(cliente.getSaldo().getDisponibleExtranjeraSaldo().add(operacionRequest.getMonto()))
                            .build()
            );

            operacionRepository.save(
                    Operacion.builder()
                            .fechaOperacion(
                                    ClockPe.getClockNow()
                            )
                            .valorVenta(tipoCambioResponse.getVenta())
                            .valorCompra(tipoCambioResponse.getCompra())
                            .ventaOperacion(valorRestaLocal)
                            .obtieneOperacion(operacionRequest.getMonto())
                            .idmonedatipoorigen(operacionRequest.getIdtipoorigen())
                            .idmonedatipocambio(operacionRequest.getIdtipocambio())
                            .cliente(
                                    Cliente.builder()
                                            .idCliente(operacionRequest.getIdcliente())
                                            .build()
                            )
                            .build()
            );

        }

    }

}

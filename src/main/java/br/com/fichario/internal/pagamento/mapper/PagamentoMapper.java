package br.com.fichario.internal.pagamento.mapper;

import br.com.fichario.internal.pagamento.dto.ExtratoPagamentosResponse.Item;
import br.com.fichario.internal.pagamento.dto.ExtratoPagamentosResponse;
import br.com.fichario.internal.pedido.entity.Pagamento;
import br.com.fichario.internal.pedido.entity.Pedido;

/** Converte o Pedido e seus pagamentos para o extrato financeiro. */
public final class PagamentoMapper {

    private PagamentoMapper() {
    }

    public static ExtratoPagamentosResponse paraExtrato(Pedido pedido) {
        return new ExtratoPagamentosResponse(
                pedido.getId(),
                pedido.getCliente().getNome(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getValorPago(),
                pedido.getSaldo(),
                pedido.getPagamentos().stream().map(PagamentoMapper::paraItem).toList()
        );
    }

    private static Item paraItem(Pagamento pagamento) {
        return new Item(
                pagamento.getId(),
                pagamento.getValor(),
                pagamento.getForma(),
                pagamento.getRegistradoEm(),
                pagamento.getRegistradoPor().getNome(),
                pagamento.getRecebidoPor(),
                pagamento.getValorOriginal(),
                pagamento.getMotivoCorrecao()
        );
    }
}

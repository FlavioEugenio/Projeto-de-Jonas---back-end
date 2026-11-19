package br.com.fichario.dto;

import br.com.fichario.domain.model.FormaPagamento;
import br.com.fichario.domain.model.Pagamento;
import br.com.fichario.domain.model.Pedido;
import br.com.fichario.domain.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Situacao financeira do pedido. O saldo e sempre calculado pelo dominio, nunca digitado. */
public record ExtratoPagamentosResponse(
        Long pedidoId,
        String cliente,
        StatusPedido status,
        BigDecimal valorTotal,
        BigDecimal valorPago,
        BigDecimal saldo,
        List<Item> pagamentos) {

    public record Item(
            Long id,
            BigDecimal valor,
            FormaPagamento forma,
            LocalDateTime registradoEm,
            String registradoPor,
            String recebidoPor,
            BigDecimal valorOriginal,
            String motivoCorrecao) {

        static Item de(Pagamento p) {
            return new Item(p.getId(), p.getValor(), p.getForma(), p.getRegistradoEm(),
                    p.getRegistradoPor().getNome(), p.getRecebidoPor(), p.getValorOriginal(), p.getMotivoCorrecao());
        }
    }

    public static ExtratoPagamentosResponse de(Pedido pedido) {
        return new ExtratoPagamentosResponse(pedido.getId(), pedido.getCliente().getNome(), pedido.getStatus(),
                pedido.getValorTotal(), pedido.getValorPago(), pedido.getSaldo(),
                pedido.getPagamentos().stream().map(Item::de).toList());
    }
}

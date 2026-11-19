package br.com.fichario.internal.pagamento.dto;

import br.com.fichario.internal.pedido.entity.FormaPagamento;
import br.com.fichario.internal.pedido.entity.StatusPedido;
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
    }
}

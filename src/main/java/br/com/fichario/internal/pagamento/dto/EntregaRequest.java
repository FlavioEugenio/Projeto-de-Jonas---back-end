package br.com.fichario.internal.pagamento.dto;

import br.com.fichario.internal.pedido.entity.FormaPagamento;

import java.math.BigDecimal;

/** Com saldo em aberto, os tres campos sao obrigatorios e o valor precisa ser o saldo completo. */
public record EntregaRequest(BigDecimal valorRecebido, FormaPagamento forma, String recebidoPor) {
}

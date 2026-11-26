package br.com.fichario.internal.pagamento.dto;

import br.com.fichario.internal.pedido.entity.FormaPagamento;

import java.math.BigDecimal;

public record EntregaRequest(BigDecimal valorRecebido, FormaPagamento forma, String recebidoPor) {
}

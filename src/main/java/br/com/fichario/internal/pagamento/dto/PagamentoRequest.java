package br.com.fichario.internal.pagamento.dto;

import br.com.fichario.internal.pedido.entity.FormaPagamento;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PagamentoRequest(
        @NotNull(message = "informe o valor") BigDecimal valor,
        @NotNull(message = "informe a forma: PIX, DINHEIRO, CARTAO ou TRANSFERENCIA") FormaPagamento forma) {
}

package br.com.fichario.internal.pagamento.dto;

import br.com.fichario.internal.pedido.entity.FormaPagamento;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Dados da entrega. Com saldo em aberto, os tres campos sao obrigatorios e o valor
 * precisa ser o saldo completo. Com o pedido ja quitado, o corpo pode ir vazio.
 */
public record EntregaRequest(
        BigDecimal valorRecebido,
        FormaPagamento forma,
        @Size(max = 100, message = "no maximo 100 caracteres")
        String recebidoPor) {
}

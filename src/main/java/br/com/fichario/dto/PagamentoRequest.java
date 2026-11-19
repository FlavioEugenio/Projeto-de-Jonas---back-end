package br.com.fichario.dto;

import br.com.fichario.domain.model.FormaPagamento;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Sinal ou pagamento parcial recebido antes da entrega. */
public record PagamentoRequest(
        @NotNull(message = "informe o valor")
        @Positive(message = "deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "use no maximo duas casas decimais")
        BigDecimal valor,

        @NotNull(message = "informe a forma: PIX, DINHEIRO, CARTAO ou TRANSFERENCIA")
        FormaPagamento forma) {
}

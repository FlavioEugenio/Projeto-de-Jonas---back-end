package br.com.fichario.internal.pagamento.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CorrecaoPagamentoRequest(
        @NotNull(message = "informe o valor corrigido")
        @Positive(message = "deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "use no maximo duas casas decimais")
        BigDecimal valor,

        @NotBlank(message = "informe o motivo da correcao")
        @Size(max = 300, message = "no maximo 300 caracteres")
        String motivo) {
}

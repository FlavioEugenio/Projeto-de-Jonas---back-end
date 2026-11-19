package br.com.fichario.internal.pagamento.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ValorTotalRequest(
        @NotNull(message = "informe o novo valor total")
        @Positive(message = "deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "use no maximo duas casas decimais")
        BigDecimal valorTotal) {
}

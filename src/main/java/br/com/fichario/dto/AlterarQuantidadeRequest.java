package br.com.fichario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AlterarQuantidadeRequest(
        @NotNull(message = "informe a quantidade")
        @Min(value = 1, message = "deve ser de 1 para cima")
        Integer quantidade) {
}

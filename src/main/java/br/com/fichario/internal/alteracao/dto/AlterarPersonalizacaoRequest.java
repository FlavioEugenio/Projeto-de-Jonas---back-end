package br.com.fichario.internal.alteracao.dto;

import jakarta.validation.constraints.NotBlank;

public record AlterarPersonalizacaoRequest(
        @NotBlank(message = "informe a nova personalizacao") String personalizacao) {
}

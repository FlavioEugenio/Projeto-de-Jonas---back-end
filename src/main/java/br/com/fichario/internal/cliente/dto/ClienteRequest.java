package br.com.fichario.internal.cliente.dto;

import jakarta.validation.constraints.NotBlank;

public record ClienteRequest(
        @NotBlank(message = "informe o nome") String nome,
        @NotBlank(message = "informe o telefone com DDD") String telefone) {
}

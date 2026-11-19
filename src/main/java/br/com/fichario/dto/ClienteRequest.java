package br.com.fichario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados para cadastrar ou atualizar um cliente. O telefone aceita mascara: (85) 98888-7777. */
public record ClienteRequest(
        @NotBlank(message = "informe o nome")
        @Size(max = 100, message = "no maximo 100 caracteres")
        String nome,

        @NotBlank(message = "informe o telefone com DDD")
        String telefone,

        @Size(max = 60, message = "no maximo 60 caracteres")
        String instagram,

        @Size(max = 500, message = "no maximo 500 caracteres")
        String observacoes) {
}

package br.com.fichario.internal.alteracao.dto;

import jakarta.validation.constraints.Size;

/** Novo texto para a personalizacao ou as observacoes. Observacoes aceitam vazio (para apagar). */
public record AlterarTextoRequest(
        @Size(max = 1000, message = "no maximo 1000 caracteres")
        String valor) {
}

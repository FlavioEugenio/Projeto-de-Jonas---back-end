package br.com.fichario.internal.alteracao.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AlterarDataEntregaRequest(
        @NotNull(message = "informe a nova data no formato AAAA-MM-DD")
        LocalDate dataEntrega) {
}

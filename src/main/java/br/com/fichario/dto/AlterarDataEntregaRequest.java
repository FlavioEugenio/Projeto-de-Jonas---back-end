package br.com.fichario.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AlterarDataEntregaRequest(
        @NotNull(message = "informe a nova data no formato AAAA-MM-DD")
        LocalDate dataEntrega) {
}

package br.com.fichario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelamentoRequest(
        @NotBlank(message = "informe o motivo do cancelamento")
        @Size(max = 300, message = "no maximo 300 caracteres")
        String motivo) {
}

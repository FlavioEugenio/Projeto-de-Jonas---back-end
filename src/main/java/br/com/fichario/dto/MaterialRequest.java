package br.com.fichario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MaterialRequest(
        @NotBlank(message = "informe o nome do material")
        @Size(max = 100, message = "no maximo 100 caracteres")
        String nome) {
}

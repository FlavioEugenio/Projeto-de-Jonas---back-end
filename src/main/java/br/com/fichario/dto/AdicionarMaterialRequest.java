package br.com.fichario.dto;

import jakarta.validation.constraints.NotNull;

public record AdicionarMaterialRequest(
        @NotNull(message = "informe o material")
        Long materialId) {
}

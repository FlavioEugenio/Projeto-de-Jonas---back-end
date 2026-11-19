package br.com.fichario.internal.material.dto;

import jakarta.validation.constraints.NotNull;

public record AdicionarMaterialRequest(
        @NotNull(message = "informe o material")
        Long materialId) {
}

package br.com.fichario.internal.pedido.dto;

import br.com.fichario.internal.pedido.entity.CanalOrigem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NovoPedidoRequest(
        @NotNull(message = "informe o cliente") Long clienteId,
        @NotNull(message = "informe o produto") Long produtoId,
        @NotNull(message = "informe a quantidade") Integer quantidade,
        @NotBlank(message = "descreva a personalizacao") String personalizacao,
        @NotNull(message = "informe a data de entrega") LocalDate dataEntrega,
        @NotNull(message = "informe o valor total") BigDecimal valorTotal,
        @NotNull(message = "informe o canal") CanalOrigem canal) {
}

package br.com.fichario.dto;

import br.com.fichario.domain.model.CanalOrigem;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Dados do pedido que o atendente registra a partir da conversa com o cliente. */
public record NovoPedidoRequest(
        @NotNull(message = "informe o cliente")
        Long clienteId,

        @NotNull(message = "informe o produto")
        Long produtoId,

        @NotNull(message = "informe a quantidade")
        @Min(value = 1, message = "deve ser de 1 para cima")
        Integer quantidade,

        @NotBlank(message = "descreva a personalizacao")
        @Size(max = 1000, message = "no maximo 1000 caracteres")
        String personalizacao,

        @NotNull(message = "informe a data de entrega")
        @FutureOrPresent(message = "nao pode estar no passado")
        LocalDate dataEntrega,

        @NotNull(message = "informe o valor total")
        @Positive(message = "deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "use no maximo duas casas decimais")
        BigDecimal valorTotal,

        @NotNull(message = "informe o canal: WHATSAPP, INSTAGRAM ou PRESENCIAL")
        CanalOrigem canal) {
}

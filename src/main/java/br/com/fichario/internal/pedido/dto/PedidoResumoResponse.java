package br.com.fichario.internal.pedido.dto;

import br.com.fichario.internal.pedido.entity.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Linha da listagem de pedidos. */
public record PedidoResumoResponse(
        Long id,
        String cliente,
        String produto,
        int quantidade,
        LocalDate dataEntrega,
        StatusPedido status,
        BigDecimal valorTotal,
        BigDecimal saldo,
        boolean alteracoesPendentes,
        boolean atrasado) {
}

package br.com.fichario.dto;

import br.com.fichario.domain.model.Pedido;
import br.com.fichario.domain.model.StatusPedido;

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

    public static PedidoResumoResponse de(Pedido pedido) {
        return new PedidoResumoResponse(
                pedido.getId(),
                pedido.getCliente().getNome(),
                pedido.getProduto().getNome(),
                pedido.getQuantidade(),
                pedido.getDataEntrega(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getSaldo(),
                pedido.possuiAlteracoesPendentes(),
                pedido.estaAtrasado(LocalDate.now()));
    }
}
